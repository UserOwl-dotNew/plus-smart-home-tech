package ru.yandex.practicum.product.service;

import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateCategoryRequest;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> findAllCategories();

    CategoryDto findCategoryById(Long id);

    Boolean existsById(Long id);

    CategoryDto createCategory(CreateCategoryRequest request);
}
