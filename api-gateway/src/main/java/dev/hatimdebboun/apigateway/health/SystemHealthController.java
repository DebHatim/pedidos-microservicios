package dev.hatimdebboun.apigateway.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost", "http://localhost:5173", "https://productos.hatimdebboun.dev"}, allowCredentials = "true")
public class SystemHealthController {

    // Internal container hostnames + the ports each service is actually configured to listen on (server.port)
    private static final Map<String, String> SERVICES = Map.of(
            "api-gateway", "http://localhost:8080/actuator/health",
            "order-service", "http://order-service:8081/actuator/health",
            "inventory-service", "http://inventory-service:8082/actuator/health",
            "notification-service", "http://notification-service:8083/actuator/health"
    );

    private final WebClient webClient = WebClient.builder().build();

    @GetMapping("/system/health")
    public Mono<ResponseEntity<List<Map<String, String>>>> systemHealth() {
        List<Mono<Map<String, String>>> checks = SERVICES.entrySet().stream()
                .map(entry -> checkService(entry.getKey(), entry.getValue()))
                .toList();

        return Flux.merge(checks)
                .collectList()
                .map(ResponseEntity::ok);
    }

    private Mono<Map<String, String>> checkService(String name, String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> Map.of("name", name, "status",
                        "UP".equals(body.get("status")) ? "up" : "down"))
                .timeout(java.time.Duration.ofSeconds(3))
                .onErrorReturn(Map.of("name", name, "status", "down"));
    }
}