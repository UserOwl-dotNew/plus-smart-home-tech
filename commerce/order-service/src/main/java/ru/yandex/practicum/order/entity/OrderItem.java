package ru.yandex.practicum.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Сущность позиции заказа.
 * Содержит информацию о товаре, его количестве и цене на момент оформления заказа.
 */
@Entity
@Table(name = "order_items")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    /**
     * Уникальный идентификатор позиции заказа.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Заказ, к которому относится позиция.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Идентификатор товара.
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * Название товара на момент оформления заказа.
     */
    @Column(name = "product_name", nullable = false)
    private String productName;

    /**
     * Количество единиц товара.
     */
    @Column(name = "quantity")
    private Integer quantity;

    /**
     * Цена за единицу товара на момент оформления заказа.
     */
    @Column(name = "price")
    private BigDecimal price;
}