package ru.yandex.practicum.order.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.service.OrderService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @GetMapping("/{id}")
    public OrderDto getOne(@PathVariable @Positive Long id) {
        log.info("GET /api/orders/{} - запрос на получение заказа", id);
        OrderDto result = service.findById(id);
        log.info("GET /api/orders/{} - возвращён заказ: {}", id, result);
        return result;
    }

    @GetMapping
    public List<OrderDto> getAll() {
        log.info("GET /api/orders - запрос на получение всех заказов");
        List<OrderDto> result = service.findAll();
        log.info("GET /api/orders - возвращено заказов: {}", result.size());
        return result;
    }

    @GetMapping("/by-email")
    public List<OrderDto> getAllForClient(@RequestParam(value = "email") @Email String email) {
        log.info("GET /api/orders/by-email - запрос на получение заказов по email: {}", email);
        List<OrderDto> result = service.findAllByEmail(email);
        log.info("GET /api/orders/by-email - для email {} возвращено заказов: {}", email, result.size());
        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(@RequestBody @Valid CreateOrderRequest request) {
        log.info("POST /api/orders - запрос на создание заказа: {}", request);
        OrderDto result = service.createOrder(request);
        log.info("POST /api/orders - создан заказ: {}", result);
        return result;
    }
}