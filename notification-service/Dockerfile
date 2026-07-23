# 1 - Compilar el proyecto
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 2 - Copiar primero el archivo pom.xml y descargar las dependencias
# Al estar separado de 'src', Docker cacheará esta capa. Si en el futuro solo cambia el código fuente, no volverá a
# descargar las dependencias.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 3 - Copiar el código fuente y empaquetar la aplicación
COPY src ./src
RUN mvn clean package -DskipTests

# 4 - Crear la imagen final
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 5 - Copiar el archivo JAR generado desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]