package co.com.pragma.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()

                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitud").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/v1/solicitud/**", "/api/v1/solicitudes/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/v1/solicitud/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(this::convertJwt))
                )
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret) {
        var key = new javax.crypto.spec.SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }

    private Mono<AbstractAuthenticationToken> convertJwt(Jwt jwt) {
        String rol = jwt.getClaimAsString("role");
        log.info("### ROL EXTRAÍDO DEL TOKEN: {}", rol);
        if (rol == null || rol.isBlank()) {
            rol = "USER";
        }
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase()));
        log.info("### AUTORIDADES GENERADAS: {}", authorities);
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }
}