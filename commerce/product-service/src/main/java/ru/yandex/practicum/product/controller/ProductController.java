package ru.yandex.practicum.product.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.service.ProductService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductDto> getAll() {
        return productService.findAllProducts();
    }

    @GetMapping("/{id}")
    public ProductDto findById(@PathVariable @Positive Long id) {
        return productService.findProductById(id);
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductDto> findByCategory(@PathVariable @Positive Long categoryId) {
        return productService.findAllProductsByCategoryId(categoryId);
    }

    @GetMapping("/search")
    public List<ProductDto> findByTitle(@RequestParam(name = "query") String name) {
        return productService.findAllProductsByTitle(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto create(@RequestBody @Valid CreateProductRequest request) {
        return productService.createProduct(request);
    }

    @PatchMapping("/{id}")
    public ProductDto update(
            @PathVariable @Positive Long id,
            @RequestBody @Valid UpdateProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }
}
