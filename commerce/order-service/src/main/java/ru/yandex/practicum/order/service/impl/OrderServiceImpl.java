package ru.yandex.practicum.order.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.enums.OrderStatus;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.repository.OrderRepository;
import ru.yandex.practicum.order.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper mapper;

    @Override
    public OrderDto findById(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new NotFoundException("Order not found with id=" + id));

        return mapper.toDto(order);
    }

    @Override
    public List<OrderDto> findAll() {
        return orderRepository.findAllWithItems().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<OrderDto> findAllByEmail(String email) {
        return orderRepository.findAllByEmailWithItems(email).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public OrderDto createOrder(CreateOrderRequest request) {
        log.info("CREATE ORDER request: {}", request);
        List<OrderItem> orderItems = request.items().stream()
                .map(req -> OrderItem.builder()
                        .productId(req.productId())
                        .productName(req.productName())
                        .quantity(req.quantity())
                        .price(req.price())
                        .build())
                .toList();
        log.info("ORDER ITEMS SIZE: {}", orderItems.size());

        BigDecimal total = getTotalPrice(orderItems);
        log.info("TOTAL PRICE: {}", total);

        Order order = Order.builder()
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .status(OrderStatus.CREATED.name())
                .createdAt(LocalDateTime.now())
                .totalPrice(total)
                .orderItems(new ArrayList<>())
                .statusDetails("string")
                .build();

        orderItems.forEach(order::addOrderItem);
        log.info("ORDER: {}", order);

        return mapper.toDto(orderRepository.save(order));
    }

    private BigDecimal getTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
