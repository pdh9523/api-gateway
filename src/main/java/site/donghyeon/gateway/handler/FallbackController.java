package site.donghyeon.gateway.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@Slf4j
public class FallbackController {
    @GetMapping("/__fallback")
    public Mono<Map<String, String>> fallback() {
        log.error("Circuit breaker fallback triggered");
        return Mono.just(Map.of("status", "fallback", "message", "Service temporarily unavailable"));
    }
}
