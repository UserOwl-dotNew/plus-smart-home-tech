package ru.yandex.practicum.product.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateCategoryRequest;
import ru.yandex.practicum.product.service.CategoryService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAll() {
        log.info("GET /api/categories - запрос на получение всех категорий");
        List<CategoryDto> result = categoryService.findAllCategories();
        log.info("GET /api/categories - возвращено категорий: {}", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public CategoryDto findById(@PathVariable @Positive Long id) {
        log.info("GET /api/categories/{} - запрос на получение категории", id);
        CategoryDto result = categoryService.findCategoryById(id);
        log.info("GET /api/categories/{} - возвращена категория: {}", id, result);
        return result;
    }

    @GetMapping("/exists/{id}")
    public Boolean existsById(@PathVariable @Positive Long id) {
        log.info("GET /api/categories/exists/{} - запрос на существование категории", id);
        Boolean result = categoryService.existsById(id);
        log.info("GET /api/categories/exists/{} - категория найдена: {}", id, result);
        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody @Valid CreateCategoryRequest request) {
        log.info("POST /api/categories - запрос на создание категории: {}", request);
        CategoryDto result = categoryService.createCategory(request);
        log.info("POST /api/categories - создана категория: {}", result);
        return result;
    }
}