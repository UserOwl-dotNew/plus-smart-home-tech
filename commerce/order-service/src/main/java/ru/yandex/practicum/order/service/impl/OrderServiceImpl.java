package ru.yandex.practicum.order.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.OIDTokenizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.dto.*;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.entity.Reservation;
import ru.yandex.practicum.order.enums.OrderStatus;
import ru.yandex.practicum.order.exception.InventoryServiceUnavailableException;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.exception.ProductServiceUnavailableException;
import ru.yandex.practicum.order.feign.InventoryClient;
import ru.yandex.practicum.order.feign.ProductClient;
import ru.yandex.practicum.order.feign.result.ServiceCallResult;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.repository.OrderRepository;
import ru.yandex.practicum.order.service.OrderSaver;
import ru.yandex.practicum.order.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    private final OrderSaver orderSaver;

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

        List<ServiceCallResult<ProductDto>> productResults = request.items().stream()
                .map(OrderItemRequest::productId)
                .distinct()
                .map(this::getProduct)
                .toList();

        boolean hasFailure = productResults.stream()
                .anyMatch(r -> r instanceof ServiceCallResult.Failure);

        if (hasFailure) throw new OrderProcessingException("Order rejected: business failure");

        boolean hasDegraded = productResults.stream()
                .anyMatch(r -> r instanceof ServiceCallResult.Degraded);

        if (hasDegraded) {
            Order pending = buildPendingOrder(request);
            return mapper.toDto(orderSaver.save(pending));
        };

        Map<Long, ProductDto> products = productResults.stream()
                .map(r -> ((ServiceCallResult.Success<ProductDto>) r).data())
                .collect(Collectors.toMap(ProductDto::id, Function.identity()));

        Map<Long, Integer> quantities = request.items().stream()
                .collect(Collectors.toMap(
                        OrderItemRequest::productId,
                        OrderItemRequest::quantity,
                        Integer::sum
                ));

        List<ServiceCallResult<Reservation>> reserveResults = new ArrayList<>();
        List<Reservation> reserved = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            ServiceCallResult<Reservation> result = reserve(productId, quantity);
            reserveResults.add(result);

            if (result instanceof ServiceCallResult.Success<Reservation>(Reservation data)) reserved.add(data);
        }

        hasFailure = reserveResults.stream()
                .anyMatch(r -> r instanceof ServiceCallResult.Failure);

        if (hasFailure) {
            releaseAll(reserved);
            throw new OrderProcessingException("Reservation failed");
        }

        hasDegraded = reserveResults.stream()
                .anyMatch(r -> r instanceof ServiceCallResult.Degraded);

        if (hasDegraded) {
            releaseAll(reserved);
            Order pending = buildPendingOrder(request);
            return mapper.toDto(orderSaver.save(pending));
        }

        try {
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

            order = orderSaver.save(order);

            return mapper.toDto(order);
        } catch (Exception e) {
            releaseAll(reserved);
            throw e instanceof OrderProcessingException ? (OrderProcessingException) e
                    : new OrderProcessingException("Order processign failed", e);
        }
    }

    private BigDecimal getTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order buildPendingOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .status(OrderStatus.PENDING_CONFIRMATION.name())
                .createdAt(LocalDateTime.now())
                .totalPrice(BigDecimal.ZERO)
                .statusDetails("Заказ требует ручной проверки")
                .orderItems(new ArrayList<>())
                .build();

        request.items().forEach(item -> {
            OrderItem orderItem = OrderItem.builder()
                    .productId(item.productId())
                    .productName("Товар #" + item.productId() + " (ожидает проверки)")
                    .quantity(item.quantity())
                    .price(BigDecimal.ZERO)
                    .build();
            order.addOrderItem(orderItem);
        });

        return order;
    }

    private void releaseAll(List<Reservation> reservations) {
        reservations.forEach(r -> {
            try {
                inventoryClient.releaseStock(new ReserveRequest(
                        r.productId(),
                        r.quantity()
                ));
            } catch (Exception ex) {
                log.warn("Failed to release reservation for productId={}, quantity={}: {}",
                        r.productId(), r.quantity(), ex.getMessage());
            }
        });
    }

    private ServiceCallResult<ProductDto> getProduct(Long productId) {
        try {
            ProductDto dto = productClient.getProductById(productId);
            if (!Boolean.TRUE.equals(dto.active()))
                return new ServiceCallResult.Failure("Товар снят с продажи");
            return new ServiceCallResult.Success<>(dto);
        } catch (FeignException.NotFound e) {
            return new ServiceCallResult.Failure("Товар не найден: " + productId);
        } catch (ProductServiceUnavailableException e) {
            return new ServiceCallResult.Degraded("Каталог не доступен");
        } catch (FeignException e) {
            return new ServiceCallResult.Degraded("Ошибка product-service: " + e.status());
        }
    }

    private ServiceCallResult<Reservation> reserve(Long productId, Integer quantity) {
        try {
            inventoryClient.reserveStock(new ReserveRequest(productId, quantity));
            return new ServiceCallResult.Success<>(new Reservation(productId, quantity));
        } catch (FeignException.NotFound e) {
            return new ServiceCallResult.Failure("Складская запись не найдена: " + productId);
        } catch (FeignException.Conflict e) {
            return new ServiceCallResult.Failure("Недостаточно товара: " + productId);
        } catch (InventoryServiceUnavailableException e) {
            return new ServiceCallResult.Degraded("Склад недоступен");
        } catch (FeignException e) {
            return new ServiceCallResult.Degraded("Ошибка inventory-service: " + e.status());
        }
    }
}
