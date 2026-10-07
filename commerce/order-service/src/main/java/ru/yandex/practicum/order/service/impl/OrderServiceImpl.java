package ru.yandex.practicum.order.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.dto.*;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.entity.Reservation;
import ru.yandex.practicum.order.enums.OrderStatus;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.feign.InventoryClient;
import ru.yandex.practicum.order.feign.ProductClient;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.repository.OrderRepository;
import ru.yandex.practicum.order.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final InventoryClient inventoryClient;
    private final ProductClient productClient;
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

    @Override
    public OrderDto createOrder(CreateOrderRequest request) {
        Map<Long, ProductDto> products;
        try {
            products = request.items().stream()
                    .map(OrderItemRequest::productId)
                    .distinct()
                    .collect(Collectors.toMap(
                            id -> id,
                            productClient::getProductById
                    ));
        } catch (FeignException.NotFound e) {
            throw new OrderProcessingException("Product not found", e);
        }

        products.forEach((id, dto) -> {
            if (!Boolean.TRUE.equals(dto.active())) throw new OrderProcessingException("Product is inactive: " + id);
        });

        Map<Long, Integer> quantities = request.items().stream()
                .collect(Collectors.toMap(
                        OrderItemRequest::productId,
                        OrderItemRequest::quantity,
                        Integer::sum
                ));

        List<Reservation> reserved = new ArrayList<>();
        try {
            quantities.forEach((id, qty) -> {
                try {
                    inventoryClient.reserveStock(new ReserveRequest(id, qty));
                    reserved.add(new Reservation(id, qty));
                } catch (FeignException.NotFound e) {
                    throw new OrderProcessingException("Inventory not found: " + id, e);
                } catch (FeignException.Conflict e) {
                    throw new OrderProcessingException("Not enough stock: " + id, e);
                } catch (FeignException e) {
                    throw new OrderProcessingException("Inventory error: " + id, e);
                }
            });

            List<OrderItem> orderItems = request.items().stream()
                    .map(req -> {
                        ProductDto product = products.get(req.productId());
                        return OrderItem.builder()
                                .productId(product.id())
                                .productName(product.name())
                                .quantity(quantities.get(req.productId()))
                                .price(product.price())
                                .build();
                    })
                    .toList();

            BigDecimal totalPrice = getTotalPrice(orderItems);

            Order order = Order.builder()
                    .customerName(request.customerName())
                    .customerEmail(request.customerEmail())
                    .status(OrderStatus.CONFIRMED.name())
                    .createdAt(LocalDateTime.now())
                    .totalPrice(totalPrice)
                    .statusDetails("Order confirmed")
                    .orderItems(new ArrayList<>())
                    .build();

            orderItems.forEach(order::addOrderItem);


            return mapper.toDto(orderRepository.save(order));
        } catch (Exception e) {
            reserved.forEach(r -> {
                try {
                    inventoryClient.releaseStock(new ReserveRequest(r.productId(), r.quantity()));
                } catch (Exception ex) {
                    log.warn("Failed to release reservation for productId={}, quantity={}: {}",
                            r.productId(), r.quantity(), ex.getMessage());
                }
            });
            throw e instanceof OrderProcessingException ? (OrderProcessingException) e
                    : new OrderProcessingException("Order processign failed", e);
        }
    }

    private BigDecimal getTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
