package ru.yandex.practicum.product.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.service.ProductService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductDto> getAll() {
        log.info("GET /api/products - запрос на получение всех товаров");
        List<ProductDto> result = productService.findAllProducts();
        log.info("GET /api/products - возвращено товаров: {}", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public ProductDto findById(@PathVariable @Positive Long id) {
        log.info("GET /api/products/{} - запрос на получение товара", id);
        ProductDto result = productService.findProductById(id);
        log.info("GET /api/products/{} - возвращён товар: {}", id, result);
        return result;
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductDto> findByCategory(@PathVariable @Positive Long categoryId) {
        log.info("GET /api/products/category/{} - запрос на получение товаров по категории", categoryId);
        List<ProductDto> result = productService.findAllProductsByCategoryId(categoryId);
        log.info("GET /api/products/category/{} - возвращено товаров: {}", categoryId, result.size());
        return result;
    }

    @GetMapping("/search")
    public List<ProductDto> findByTitle(@RequestParam(name = "query") String name) {
        log.info("GET /api/products/search - запрос на поиск товаров по названию: {}", name);
        List<ProductDto> result = productService.findAllProductsByTitle(name);
        log.info("GET /api/products/search - по запросу '{}' найдено товаров: {}", name, result.size());
        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto create(@RequestBody @Valid CreateProductRequest request) {
        log.info("POST /api/products - запрос на создание товара: {}", request);
        ProductDto result = productService.createProduct(request);
        log.info("POST /api/products - создан товар: {}", result);
        return result;
    }

    @PatchMapping("/{id}")
    public ProductDto update(
            @PathVariable @Positive Long id,
            @RequestBody @Valid UpdateProductRequest request
    ) {
        log.info("PATCH /api/products/{} - запрос на обновление товара: {}", id, request);
        ProductDto result = productService.updateProduct(id, request);
        log.info("PATCH /api/products/{} - обновлён товар: {}", id, result);
        return result;
    }
}