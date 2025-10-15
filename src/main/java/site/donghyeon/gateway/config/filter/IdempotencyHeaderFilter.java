package site.donghyeon.gateway.config.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class IdempotencyHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (isNonIdempotentMethod(request.getMethod())) {
            String idempotencyKey = request.getHeaders().getFirst("Idempotency-Key");
            if (StringUtils.hasText(idempotencyKey)) {
                log.debug("Idempotency-Key header is required, {}", request.getHeaders().getFirst("traceparent"));
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                return exchange.getResponse().setComplete();
            }
        }
        return chain.filter(exchange);
    }

    private boolean isNonIdempotentMethod(HttpMethod httpMethod) {
        return httpMethod == HttpMethod.POST ||
                httpMethod == HttpMethod.PUT ||
                httpMethod == HttpMethod.PATCH;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
