package ru.yandex.practicum.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateCategoryRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.exception.DuplicateException;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.mapper.CategoryMapper;
import ru.yandex.practicum.product.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    @Override
    public List<CategoryDto> findAllCategories() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public CategoryDto findCategoryById(Long id) {
        Category category = repository.findById(id).orElseThrow(() -> new NotFoundException("Category not found with id=" + id));
        return mapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CreateCategoryRequest request) {
        if (repository.existsByName(request.name())) {
            throw new DuplicateException("Category exists with name=" + request.name());
        }
        return mapper.toDto(repository.save(mapper.toEntity(request)));
    }
}
