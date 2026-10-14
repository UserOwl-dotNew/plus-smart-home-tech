package ru.yandex.practicum.order.feign.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import ru.yandex.practicum.order.dto.ReserveRequest;
import ru.yandex.practicum.order.dto.ReserveResponse;
import ru.yandex.practicum.order.exception.InventoryServiceUnavailableException;
import ru.yandex.practicum.order.feign.InventoryClient;

public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryClientFallbackFactory.class);

    @Override
    public InventoryClient create(Throwable cause) {
        return new InventoryClient() {
            @Override
            public ReserveResponse reserveStock(ReserveRequest request) {
                log.warn(
                        "inventory-service недоступен при резервировании товара id={}",
                        request.productId(),
                        cause
                );

                throw new InventoryServiceUnavailableException(request.productId(), cause);
            }

            @Override
            public ReserveResponse releaseStock(ReserveRequest request) {
                log.warn(
                        "inventory-service недоступен при снятии резерва товара id={}",
                        request.productId(),
                        cause
                );

                throw new InventoryServiceUnavailableException(request.productId(), cause);
            }
        };
    }
}
