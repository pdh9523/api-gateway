package site.donghyeon.gateway.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public Mono<Map<String, String>> fallback() {
        log.error("Circuit breaker fallback triggered");
        return Mono.just(Map.of("status", "fallback", "message", "Service temporarily unavailable"));
    }
}