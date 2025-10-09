package site.donghyeon.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/test/**").permitAll()
                        // 권한 기반 라우팅 예시
                        .pathMatchers("/book/**").hasRole("ADMIN")
                        .pathMatchers("/search/**").hasAnyRole("USER", "ADMIN")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .jwtAuthenticationConverter(jwtRoleConverter())  // ← 커스텀 Converter 등록
                ))
                .build();
    }

    @Bean
    public JwtRoleConverter jwtRoleConverter() {
        return new JwtRoleConverter();
    }
}
