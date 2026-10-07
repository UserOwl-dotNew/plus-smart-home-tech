package ru.yandex.practicum.product.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.product.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(@NotBlank(message = "Название категории обязательно") @Size(max = 255, message = "Название категории не может быть длиннее 255 символов") String name);
}
