package ru.yandex.practicum.order.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.entity.Order;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final ItemMapper itemMapper;

    public OrderDto toDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getStatusDetails(),
                order.getCreatedAt(),
                order.getOrderItems().stream().map(itemMapper::toItemDto).toList()
        );
    }
}
