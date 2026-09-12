package ru.yandex.practicum.analyzer.config;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Configuration
public class GrpcClientConfig {

    @Bean
    public HubRouterControllerGrpc.HubRouterControllerBlockingStub hubrouterClient(
            @GrpcClient("hub-router") HubRouterControllerGrpc.HubRouterControllerBlockingStub stub) {
        return stub;
    }
}
