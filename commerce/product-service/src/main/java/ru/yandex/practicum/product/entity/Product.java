package ru.yandex.practicum.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Сущность товара.
 * Содержит информацию о названии, цене, категории и доступности товара.
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /**
     * Уникальный идентификатор товара.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название товара.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Описание товара.
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Цена товара.
     */
    @Column(name = "price")
    private BigDecimal price;

    /**
     * Категория, к которой относится товар.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Ссылка на изображение товара.
     */
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * Признак активности товара (доступен ли он для заказа).
     */
    @Column(name = "active")
    private Boolean active;
}