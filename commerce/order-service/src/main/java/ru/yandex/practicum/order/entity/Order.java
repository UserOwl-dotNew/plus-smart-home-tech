package ru.yandex.practicum.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сущность заказа клиента.
 * Хранит информацию о заказчике, статусе заказа и связанных позициях.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Order {

    /**
     * Уникальный идентификатор заказа.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя клиента, оформившего заказ.
     */
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    /**
     * Email клиента, оформившего заказ.
     */
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    /**
     * Текущий статус заказа.
     */
    @Column(name = "status", nullable = false)
    private String status;

    /**
     * Итоговая стоимость заказа.
     */
    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    /**
     * Детали текущего статуса заказа.
     */
    @Column(name = "status_details", nullable = false)
    private String statusDetails;

    /**
     * Дата и время создания заказа.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Позиции заказа.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    /**
     * Добавляет позицию к заказу и устанавливает обратную связь.
     *
     * @param item позиция заказа
     */
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
}