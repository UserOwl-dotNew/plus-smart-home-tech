package ru.yandex.practicum.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.product.entity.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p " +
            "FROM Product p " +
            "JOIN FETCH p.category " +
            "WHERE p.category.id = :categoryId AND p.active IS TRUE"
    )
    List<Product> findAllByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT p " +
            "FROM Product p " +
            "JOIN FETCH p.category " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active IS TRUE"
    )
    List<Product> findAllByTitle(@Param("name") String title);

    List<Product> findAllActiveTrue();
}
