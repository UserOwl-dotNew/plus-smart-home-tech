package ru.yandex.practicum.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.entity.Product;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.mapper.ProductMapper;
import ru.yandex.practicum.product.repository.CategoryRepository;
import ru.yandex.practicum.product.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class ProductServiceImpl implements ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Override
    public List<ProductDto> findAllProducts() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public ProductDto findProductById(Long id) {
        Product product = repository.findById(id).orElseThrow(() -> new NotFoundException("Product not found with id=" + id));
        return mapper.toDto(product);
    }

    @Override
    public List<ProductDto> findAllProductsByCategoryId(Long categoryId) {
        return repository.findAllByCategoryId(categoryId).stream().map(mapper::toDto).toList();
    }

    @Override
    public List<ProductDto> findAllProductsByTitle(String name) {
        return repository.findAllByTitle(name).stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        Category category = categoryRepository.findById(
                request.categoryId()).orElseThrow(() ->
                new NotFoundException("Category not found with id=" + request.categoryId()
                )
        );
        return mapper.toDto(repository.save(mapper.toEntity(request, category)));
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductRequest request) {
        Product product = repository.findById(id).orElseThrow(() -> new NotFoundException("Product not found with id=" + id));

        Product updateProduct = mapper.updateProduct(request, product);

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId()).orElseThrow(
                    () -> new NotFoundException("Category not found with id=" + request.categoryId())
            );

            updateProduct.setCategory(category);
        }

        return mapper.toDto(repository.save(updateProduct));
    }
}
