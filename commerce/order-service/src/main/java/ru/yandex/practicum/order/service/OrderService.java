package ru.yandex.practicum.order.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;

import java.util.List;

public interface OrderService {
    OrderDto findById(@Positive Long id);

    List<OrderDto> findAll();

    List<OrderDto> findAllByEmail(String email);

    OrderDto createOrder(@Valid CreateOrderRequest request);
}
