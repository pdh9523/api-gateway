package site.donghyeon.gateway.config;

import io.github.resilience4j.core.lang.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Keycloak JWT에서 role을 추출하여 Spring Security의 GrantedAuthority로 변환
 * Keycloak JWT 구조:
 * {
 *   "realm_access": { "roles": ["user", "admin"] },
 *   "resource_access": {
 *     "{realm}": { "roles": ["user", "admin"] }
 *   }
 * }
 */
public class JwtRoleConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    public JwtRoleConverter() {
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(@NonNull Jwt jwt) {
        // 1. Realm-level roles 추출
        Collection<GrantedAuthority> realmRoles = extractRealmRoles(jwt);

        // 2. Resource-level roles 추출
        Collection<GrantedAuthority> resourceRoles = extractResourceRoles(jwt);

        // 3. 통합
        Set<GrantedAuthority> authorities = Stream.concat(
            realmRoles.stream(),
            resourceRoles.stream()
        ).collect(Collectors.toSet());

        // 4. JwtAuthenticationToken 생성 (이것이 SecurityContext에 저장됨)
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }

    /**
     * realm_access.roles 추출
     */
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        if (realmAccess == null) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get(ROLES_CLAIM);
        if (roles == null) {
            return Collections.emptyList();
        }

        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
            .collect(Collectors.toList());
    }

    /**
     * resource_access.{client-id}.roles 추출
     */
    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim(RESOURCE_ACCESS_CLAIM);
        if (resourceAccess == null) {
            return Collections.emptyList();
        }

        Stream<Map.Entry<String, Object>> stream = resourceAccess.entrySet().stream();

        return stream
            .map(Map.Entry::getValue)
            .filter(resource -> resource instanceof Map)
            .flatMap(resource -> {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) ((Map<String, Object>) resource).get(ROLES_CLAIM);
                return roles != null ? roles.stream() : Stream.empty();
            })
            .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
            .collect(Collectors.toList());
    }
}