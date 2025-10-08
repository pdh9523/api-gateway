package site.donghyeon.gateway.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@Slf4j
public class FallbackController {
    @GetMapping("/__fallback")
    public Mono<Map<String, String>> fallback(ServerWebExchange exchange) {
        log.warn("Circuit breaker fallback triggered for request: {}", exchange.getRequest().getId());
        return Mono.just(Map.of("status", "fallback", "message", "Service temporarily unavailable"));
    }
}
