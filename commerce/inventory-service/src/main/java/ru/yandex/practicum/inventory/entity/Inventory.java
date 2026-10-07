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

    /**
     * Id товара
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * Общее количество товара на складе
     */
    @Column(name = "quantity")
    private Integer quantity;

    /**
     * Зарезервированное количество
     */
    @Column(name = "reserved_quantity")
    private Integer reservedQuantity;

    @Version
    private Long version;

    /**
     * Доступное количество, которое вычисляется как quantity - reservedQuantity
     */
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
}
