package ru.yandex.practicum.api_gateway.security.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@TestConfiguration
public class TestBackendConfig {

    @Bean
    RouterFunction<ServerResponse> testBackendRoutes() {
        return route(path("/api/products/**"), req -> ServerResponse.ok().build())
                .andRoute(path("/api/orders/**"), req -> ServerResponse.ok().build())
                .andRoute(path("/api/categories/**"), req -> ServerResponse.ok().build())
                .andRoute(path("/api/inventory/**"), req -> ServerResponse.ok().build());
    }
}
