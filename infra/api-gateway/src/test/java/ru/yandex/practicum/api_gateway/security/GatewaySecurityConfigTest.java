package ru.yandex.practicum.api_gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.yandex.practicum.gateway.ApiGatewayApp;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.gateway.globalcors.add-to-simple-url-handler-mapping=true"
        },
        classes = {ApiGatewayApp.class, ru.yandex.practicum.api_gateway.security.config.TestBackendConfig.class}
)
@AutoConfigureWebTestClient
public class GatewaySecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void catalogGet_isPublic() {
        webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isOk();
        // TODO: проверьте, что GET /api/products доступен без учётных данных
    }

    @Test
    void orderCreate_withoutCredentials_isUnauthorized() {
        webTestClient.post()
                .uri("/api/orders")
                .exchange()
                .expectStatus().isUnauthorized();
        // TODO: проверьте, что POST /api/orders без учётных данных возвращает 401
    }

    @Test
    void productWrite_withUserCredentials_isForbidden() {
        webTestClient.post()
                .uri("/api/products")
                .header(HttpHeaders.AUTHORIZATION, basic("ivan", "ivan"))
                .exchange()
                .expectStatus().isForbidden();
        // TODO: проверьте, что USER не может выполнять write-операцию с каталогом
    }

    @Test
    void productWrite_withAdminCredentials_passesSecurity() {
        webTestClient.post()
                .uri("/api/products")
                .header(HttpHeaders.AUTHORIZATION, basic("anna", "anna"))
                .exchange()
                .expectStatus().isOk();
        // TODO: проверьте, что ADMIN проходит security-проверку для write-операции
    }

    @Test
    void unknownRoute_withAdminCredentials_isForbidden() {
        webTestClient.get()
                .uri("/api/unknown")
                .header(HttpHeaders.AUTHORIZATION, basic("anna", "anna"))
                .exchange()
                .expectStatus().isForbidden();
        // TODO: проверьте, что неизвестный маршрут закрыт даже для ADMIN
    }

//    @Test
//    void corsPreflight_isPublic() {
//        webTestClient.options()
//                .uri("/api/orders")
//                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
//                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
//                .exchange()
//                .expectStatus().isOk();
//        // TODO: проверьте, что OPTIONS /api/orders не блокируется security-слоем
//    }

    private String basic(String username, String password) {
        String value = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    @TestConfiguration
    static class TestBackendConfig {

        @Bean
        RouterFunction<ServerResponse> testBackendRoutes() {
            return route(path("/test-backend"), request -> ServerResponse.ok().build());
        }


    }
}
