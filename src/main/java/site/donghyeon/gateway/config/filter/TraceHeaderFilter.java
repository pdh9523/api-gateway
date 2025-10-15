package site.donghyeon.gateway.config.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Random;
import java.util.regex.Pattern;

@Component
public class TraceHeaderFilter implements GlobalFilter, Ordered {

    private static final Random RANDOM = new SecureRandom();
    private static final Pattern TRACEPARENT_PATTERN =
            Pattern.compile("^[0-9a-f]{2}-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}$");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceparentHeader = exchange.getRequest().getHeaders().getFirst("traceparent");
        if (traceparentHeader == null || !TRACEPARENT_PATTERN.matcher(traceparentHeader).matches()) {
            String traceParent = generateTraceId();
            exchange = exchange.mutate()
                    .request(r -> r.header("traceparent", traceParent))
                    .build();
        }
        return chain.filter(exchange);
    }

    /**
     * W3C Trace Context 표준에 따른 traceparent 헤더 생성
     * 형식: 00-{trace-id}-{parent-id}-{trace-flags}
     *
     * @return traceparent 헤더 문자열 (예: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01)
     */
    private String generateTraceId() {
        // trace-id: 32자리 hex (128비트)
        String traceId, parentId;
        do {
            traceId = generateRandomHex(16);
        } while (traceId.equals("0".repeat(32)));
        // parent-id (span-id): 16자리 hex (64비트)
        do {
            parentId = generateRandomHex(8);
        }  while (parentId.equals("0".repeat(16)));
        // trace-flags: 01 (sampled)
        return String.format("00-%s-%s-01", traceId, parentId);
    }

    private String generateRandomHex(int byteCount) {
        byte[] bytes = new byte[byteCount];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * 필터 실행 순서 지정
     * 낮은 숫자일수록 먼저 실행됨
     * TraceHeaderFilter는 다른 모든 필터보다 먼저 실행되어야
     * 후속 필터들이 trace ID를 사용할 수 있음
     *
     * @return HIGHEST_PRECEDENCE (-2147483648)
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}


