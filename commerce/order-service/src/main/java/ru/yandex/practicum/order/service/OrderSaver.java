package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.repository.OrderRepository;

@Component
@RequiredArgsConstructor
public class OrderSaver {

    private final OrderRepository repository;

    @Transactional
    public Order save(Order order) {
        return repository.save(order);
    }
}
