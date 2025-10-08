package site.donghyeon.gateway.handler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {
    @GetMapping("/__fallback")
    public Mono<Map<String, String>> fallback() {
        System.out.println("check fallback");
        return Mono.just(Map.of("status", "fallback", "message", "Service temporarily unavailable"));
    }
}
