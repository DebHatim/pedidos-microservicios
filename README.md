[![English](https://img.shields.io/badge/Language-English-blue)](README.en.md) [![Español](https://img.shields.io/badge/Idioma-Español-red)](#)

---

# Sistema de Pedidos e Inventario en Tiempo Real

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud-Gateway-6DB33F)](https://spring.io/projects/spring-cloud-gateway)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft-black)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-Rate%20Limiting-DC382D)](https://redis.io/)
[![React](https://img.shields.io/badge/React-18-61DAFB)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)](https://www.docker.com/)
[![Resilience4j](https://img.shields.io/badge/Resilience-Resilience4j-orange)]()
[![OpenAPI](https://img.shields.io/badge/API%20Docs-Swagger-85EA2D)]()
[![Portfolio](https://img.shields.io/badge/Portfolio-hatimdebboun.dev-emerald)](https://hatimdebboun.dev)

Plataforma de e-commerce basada en microservicios donde un usuario crea un pedido, el sistema reserva el stock de forma
asíncrona y le notifica en tiempo real si el pedido se confirma o se rechaza por falta de existencias. Arquitectura
orientada a eventos con Apache Kafka como núcleo de comunicación entre servicios, gateway centralizado con resiliencia
(rate limiting, retries, circuit breaker) y notificaciones push vía WebSocket.

---

## Demo

![Demostración del flujo de pedidos en tiempo real](assets/screenshots/demo.gif)

*Añadir productos al carrito, confirmar el pedido y ver la notificación de confirmación/rechazo llegar en tiempo real
vía WebSocket en el momento en que inventory-service evalúa el stock disponible.*

## Arquitectura

![Diagrama de Arquitectura](assets/arquitectura-es.svg)

Cuatro servicios independientes, cada uno con su propia responsabilidad y (cuando aplica) su propia base de datos:

- **api-gateway**: punto de entrada único. Enruta `/api/orders/**` a order-service, `/api/products/**` a
  inventory-service y `/ws/**` a notification-service. Aplica rate limiting (Redis), reintentos y circuit breaker por
  ruta.
- **order-service**: crea el pedido en estado `PENDING`, lo persiste en su propia base MySQL y publica el evento
  `order-created` en Kafka. Escucha `order-evaluated` para actualizar el pedido a `CONFIRMED` o `REJECTED`.
- **inventory-service**: mantiene el catálogo de productos y su stock en su propia base MySQL. Escucha
  `order-created`, comprueba stock disponible, lo descuenta si es suficiente y publica `order-evaluated`.
- **notification-service**: puente entre Kafka y el cliente. Escucha `order-evaluated` y reenvía la notificación al
  frontend por WebSocket/STOMP, sin base de datos propia.

La comunicación entre order-service e inventory-service es siempre asíncrona vía Kafka: si inventory-service cae,
order-service sigue aceptando pedidos con normalidad y los eventos se acumulan hasta que el consumidor vuelve a estar
disponible.

## Stack

| Capa              | Tecnología                                                                 |
|-------------------|----------------------------------------------------------------------------|
| Backend           | Java 21 · Spring Boot 4.1                                                  |
| Gateway           | Spring Cloud Gateway (WebFlux)                                             |
| Mensajería        | Apache Kafka (modo KRaft, sin Zookeeper)                                   |
| Resiliencia       | Resilience4j (circuit breaker) · Retry · Rate limiting (Redis)             |
| Persistencia      | JPA/Hibernate · MySQL 8 (una base de datos por servicio)                   |
| Tiempo real       | WebSocket · STOMP                                                          |
| Documentación API | springdoc-openapi (Swagger UI) por servicio                                |
| Observabilidad    | Spring Boot Actuator · Micrometer · Prometheus · Grafana · Jaeger (OTLP)   |
| Frontend          | React 18 · Vite · @stomp/stompjs                                           |
| Infraestructura   | Docker Compose (4 microservicios, 2 MySQL, Kafka, Redis, frontend + nginx) |
| Build             | Maven · Lombok                                                             |

## Funcionalidades

- Catálogo de productos con filtro por categoría, gauge visual de stock y estados de carga/error/vacío
- Carrito de compra con control de cantidades por stock disponible y creación de pedido (`POST /api/orders`)
- Consulta del estado de un pedido por id (`GET /api/orders/{id}`)
- Evaluación de stock en tiempo real mediante consumidor Kafka en inventory-service, con descuento atómico de stock
- Notificaciones instantáneas de confirmación/rechazo del pedido al frontend vía WebSocket/STOMP, sin necesidad de
  refrescar la página
- Gateway centralizado con rate limiting por IP (Redis), reintentos automáticos en rutas GET y circuit breaker
  configurado por servicio de destino
- Reposición manual de stock por producto (`POST /api/products/{id}/stock`)
- Documentación de API interactiva vía Swagger UI en cada microservicio
- Métricas expuestas por Actuator/Prometheus y trazas distribuidas vía OpenTelemetry/Jaeger, visualizables en Grafana
- Diseño visual propio (paleta sage/crema) servido en producción mediante Dockerfile multi-stage + nginx

## Capturas de pantalla

<table>
  <tr>
    <td style="width: 50%; text-align: center;">
      <img src="assets/screenshots/catalogo.png" alt="Catálogo de productos con filtro por categoría" />
      <p><em>Catálogo: filtro por categoría, gauge de stock y estados de carga</em></p>
    </td>
    <td style="width: 50%; text-align: center;">
      <img src="assets/screenshots/carrito.png" alt="Carrito de pedido con confirmación en tiempo real" />
      <p><em>Carrito: gestión de cantidades y confirmación del pedido en tiempo real</em></p>
    </td>
  </tr>
</table>

## Decisiones de diseño

**¿Por qué microservicios y no un monolito?**
Decisión consciente para aprender y demostrar el patrón, no porque el dominio (pedidos + inventario) lo exija por
tamaño. Se gana aislamiento de fallos y escalado independiente por servicio; se pierde consistencia transaccional
fuerte entre servicios, resuelta aquí con consistencia eventual vía Kafka. Para un proyecto pequeño con un solo
equipo, un monolito bien hecho sigue siendo la decisión correcta la mayoría de las veces y este proyecto es una
excepción deliberada, orientada a demostrar el patrón.

**¿Por qué Kafka y no una llamada REST directa entre order-service e inventory-service?**
El desacoplamiento permite que ambos servicios evolucionen de forma independiente. Si inventory-service cae, los
pedidos se siguen aceptando y los eventos se acumulan en Kafka hasta que el consumidor vuelve, sin perder ninguno.

**¿Por qué una base de datos por servicio en vez de una compartida?**
Cada servicio es dueño exclusivo de sus datos. Evita acoplamiento a nivel de esquema entre order-service e
inventory-service y permite migrar o escalar cada base de forma independiente.

**¿Por qué KRaft en vez de Zookeeper?**
Menos piezas móviles en el docker-compose y es la dirección en la que Kafka está migrando por defecto; no tiene
sentido introducir una dependencia que el propio proyecto Kafka está deprecando.

**¿Por qué WebSocket/STOMP y no polling para las notificaciones?**
El polling requeriría que el cliente pregunte cada X segundos si el pedido cambió de estado, generando carga
innecesaria en el gateway. WebSocket mantiene una conexión abierta y notification-service empuja la notificación en
el momento exacto en que inventory-service evalúa el pedido.

**¿Por qué circuit breaker en el gateway y no en cada servicio individual?**
El gateway es el único punto por el que pasa todo el tráfico externo; centralizar ahí la protección evita duplicar la
misma configuración de resiliencia en cuatro sitios distintos y permite fallar rápido antes de saturar un servicio
que ya está degradado.

**¿Por qué tracing distribuido si ya hay métricas agregadas?**
Las métricas dicen que algo va mal (latencia alta, tasa de error); el tracing distribuido dice dónde exactamente
dentro de la cadena order-service → Kafka → inventory-service → Kafka → notification-service se está produciendo el
problema, algo que un dashboard de métricas agregadas no puede mostrar por sí solo.

**Lecciones aprendidas / troubleshooting**

- `.ignoreTypeHeaders()` es obligatorio en el `JacksonJsonDeserializer` al consumir eventos publicados por otro
  servicio. Por defecto, Kafka usa el header `__TypeId__` para resolver la clase de destino, y ese header contiene el
  nombre completo de la clase del *productor*, que no existe en el classpath del *consumidor*. Omitir un consumidor
  sin este flag provoca fallos de deserialización silenciosos que acaban en el dead-letter topic tras los reintentos.
- `@EnableKafka` no se auto-configura solo con tener `spring-kafka` en el classpath. En Spring Boot 4.1.0 los
  `@KafkaListener` no se registraban pese a que el `ConcurrentKafkaListenerContainerFactory` estaba correctamente
  definido como bean. Sin logs de error, sin excepciones al arrancar el contexto: los consumidores simplemente nunca
  se activaban, dejando los pedidos permanentemente en `PENDING`. Diagnosticado comparando logs de arranque entre
  servicios (el productor sí logueaba, el consumidor no dejaba ni rastro con `DEBUG` activado) y confirmando con
  `kafka-consumer-groups.sh --list` que el consumer group nunca llegaba a registrarse en el broker.

## Dependencias principales

**api-gateway**

| Dependencia                                              | Propósito                                               |
|----------------------------------------------------------|---------------------------------------------------------|
| spring-boot-starter-webflux                              | Stack reactivo sobre el que corre Spring Cloud Gateway  |
| spring-cloud-starter-gateway-server-webflux              | Enrutamiento reactivo hacia los microservicios          |
| spring-cloud-starter-circuitbreaker-reactor-resilience4j | Circuit breaker por ruta                                |
| spring-boot-starter-data-redis-reactive                  | Backend de Redis para el rate limiter del gateway       |
| resilience4j-spring-boot4                                | Configuración de resiliencia (circuit breaker, retry)   |
| micrometer-registry-prometheus                           | Métricas del gateway en formato Prometheus vía Actuator |
| micrometer-tracing-bridge-otel                           | Trazas del gateway hacia Jaeger vía OTLP                |
| springdoc-openapi-starter-webflux-ui                     | Swagger UI agregando la documentación de los servicios  |

**order-service / inventory-service**

| Dependencia                         | Propósito                                               |
|-------------------------------------|---------------------------------------------------------|
| spring-boot-starter-web             | Capa REST sobre Tomcat embebido                         |
| spring-boot-starter-data-jpa        | Persistencia JPA/Hibernate contra MySQL                 |
| spring-boot-starter-validation      | Validación de DTOs de entrada (`@Valid`)                |
| spring-kafka                        | Producción/consumo de eventos con Apache Kafka          |
| mysql-connector-j                   | Driver JDBC de MySQL                                    |
| resilience4j-spring-boot4           | Anotaciones de circuit breaker, retry y timeout         |
| micrometer-registry-prometheus      | Métricas en formato Prometheus vía Actuator             |
| micrometer-tracing-bridge-otel      | Trazas distribuidas hacia Jaeger vía OTLP               |
| springdoc-openapi-starter-webmvc-ui | Swagger UI generado a partir del código                 |
| spring-kafka-test                   | Utilidades para tests de integración con Kafka embebido |

**notification-service**

| Dependencia                         | Propósito                                               |
|-------------------------------------|---------------------------------------------------------|
| spring-boot-starter-web             | Capa REST básica y arranque del servidor embebido       |
| spring-boot-starter-websocket       | Endpoint STOMP sobre WebSocket para notificaciones push |
| spring-kafka                        | Consumo del evento `order-evaluated`                    |
| resilience4j-spring-boot4           | Resiliencia en el consumidor Kafka                      |
| micrometer-registry-prometheus      | Métricas en formato Prometheus vía Actuator             |
| micrometer-tracing-bridge-otel      | Trazas distribuidas hacia Jaeger vía OTLP               |
| springdoc-openapi-starter-webmvc-ui | Swagger UI generado a partir del código                 |

## Probarlo en local

**Requisito único:** tener Docker instalado.

```bash
git clone https://github.com/DebHatim/pedidos-inventario-microservicios.git
cd pedidos-inventario-microservicios
docker compose up -d
```

Ese único comando levanta las 2 bases MySQL, Redis, Kafka, los 4 microservicios y el frontend. Sin necesidad de
instalar Java, Maven ni Node.

- Frontend: `http://localhost`
- API Gateway: `http://localhost:8080`
- Swagger UI (por servicio, vía gateway o puerto directo): `http://localhost:8081/swagger-ui.html` (order-service),
  `http://localhost:8082/swagger-ui.html` (inventory-service)
- Health check: `http://localhost:8080/actuator/health`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Jaeger UI: `http://localhost:16686`

> Las bases de datos MySQL corren con usuario `root` y contraseña `root`, configuración pensada solo para desarrollo
> local, no para un despliegue expuesto a internet.

<details>
<summary>Desarrollo de un microservicio sin Docker (opcional)</summary>

Si quieres iterar directamente sobre un servicio con Maven, necesitas Java 21, Maven, y Kafka + MySQL corriendo (puedes
levantar solo la infraestructura con `docker compose up -d kafka redis order-mysql inventory-mysql`).

```bash
cd order-service
./mvnw spring-boot:run
```

Y para el frontend:

```bash
cd frontend
npm install
npm run dev
```

Variable de entorno relevante: `VITE_API_BASE_URL` (frontend, build-time, por defecto `http://localhost:8080`).
</details>

## Testing

> Suite de tests actualmente en construcción. Objetivo: cobertura de la lógica de negocio con JUnit 5 + Mockito, y
> tests de integración con Testcontainers (Kafka + MySQL) para order-service e inventory-service.

```bash
./mvnw test
```

## Autor

**Hatim Debboun** · [Portfolio](https://hatimdebboun.dev) · [LinkedIn](https://linkedin.com/in/hatimdebboun) · [GitHub](https://github.com/DebHatim)
