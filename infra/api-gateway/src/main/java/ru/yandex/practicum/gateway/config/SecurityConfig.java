package ru.yandex.practicum.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityUsersProperties.class)
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/inventory/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/orders/**").hasRole("USER")
                        .pathMatchers(HttpMethod.GET, "/api/orders/by-email/**").hasRole("USER")
                        .pathMatchers(HttpMethod.GET, "/api/orders/{id}").hasRole("USER")
                        .pathMatchers("/api/products/**").hasRole("ADMIN")
                        .pathMatchers("/api/categories/**").hasRole("ADMIN")
                        .pathMatchers("/api/inventory/**").hasRole("ADMIN")
                        .pathMatchers("/api/orders").hasRole("ADMIN")
                        .anyExchange().denyAll()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .build();
    }

    @Bean
    MapReactiveUserDetailsService userDetailsService(SecurityUsersProperties properties,
                                                     PasswordEncoder passwordEncoder) {
        var users = properties.getUsers().stream()
                .map(u -> User.withUsername(u.getUsername())
                        .password(passwordEncoder.encode(u.getPassword()))
                        .roles(u.getRoles().toArray(new String[0]))
                        .build())
                .toList();

        return new MapReactiveUserDetailsService(users);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
