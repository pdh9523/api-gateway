package site.donghyeon.gateway.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@Slf4j
public class FallbackController {
    /**
     * Provides a fixed fallback HTTP response used when a circuit breaker is triggered.
     *
     * The response map contains keys "status" and "message" describing the fallback state.
     *
     * @return a map with "status" -> "fallback" and "message" -> "Service temporarily unavailable"
     */
    @GetMapping("/__fallback")
    public Mono<ResponseEntity<Map<String,String>>> fallback(ServerWebExchange exchange) {
        log.warn("Circuit breaker fallback triggered for request: {}", exchange.getRequest().getId());
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(
                                Map.of(
                                        "status", "fallback",
                                        "message", "Service temporarily unavailable"
                                )
                        )
        );
    }
}