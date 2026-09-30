package ru.yandex.practicum.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventories")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * Общее количество товара на складе
     */
    @Column(name = "quantity")
    @Builder.Default
    private Integer quantity = 0;

    /**
     * Зарезервированное количество
     */
    @Column(name = "reserved_quantity")
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Version
    private Long version;

    /**
     * Доступное количество, которое вычисляется как quantity - reservedQuantity
     */
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
}
