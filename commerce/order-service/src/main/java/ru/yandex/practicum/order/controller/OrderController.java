package ru.yandex.practicum.order.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.service.OrderService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @GetMapping("/{id}")
    public OrderDto getOne(@PathVariable @Positive Long id) {
        return service.findById(id);
    }

    @GetMapping
    public List<OrderDto> getAll() {
        return service.findAll();
    }

    @GetMapping("/by-email")
    public List<OrderDto> getAllForClient(@RequestParam(value = "email") @Email String email) {
        return service.findAllByEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(@RequestBody @Valid CreateOrderRequest request) {
        return service.createOrder(request);
    }
}
