package ru.yandex.practicum.product.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;

import java.util.List;

public interface ProductService {
    List<ProductDto> findAllProducts();

    ProductDto findProductById(Long id);

    List<ProductDto> findAllProductsByCategoryId(@Positive Long categoryId);

    List<ProductDto> findAllProductsByTitle(String title);

    ProductDto createProduct(@Valid CreateProductRequest request);

    ProductDto updateProduct(@Positive Long id, @Valid UpdateProductRequest request);
}
