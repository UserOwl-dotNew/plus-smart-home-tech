package ru.yandex.practicum.inventory.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.Inventory;
import ru.yandex.practicum.inventory.exception.DuplicateException;
import ru.yandex.practicum.inventory.exception.InsufficientStockException;
import ru.yandex.practicum.inventory.exception.NotFoundException;
import ru.yandex.practicum.inventory.mapper.InventoryMapper;
import ru.yandex.practicum.inventory.repository.InventoryRepository;
import ru.yandex.practicum.inventory.service.InventoryService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repository;
    private final InventoryMapper mapper;

    @Override
    public List<InventoryDto> getInventories() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public InventoryDto getRemains(Long productId) {
        return mapper.toDto(repository.findByProductId(productId).orElseThrow(
                () -> new NotFoundException("Inventory not exists with productId=" + productId)
        ));
    }

    @Override
    public InventoryDto createInventory(UpdateInventoryRequest request) {
        Optional<Inventory> inventory = repository.findByProductId(request.productId());

        if (inventory.isPresent()) {
            throw new DuplicateException("Inventory exists with productId=" + request.productId());
        } else {
            log.info("isEmpty");
            inventory = Optional.of(repository.save(mapper.toEntity(request)));
            log.info("inventory={}", inventory.get());
        }

        return mapper.toDto(repository.save(inventory.get()));
    }

    @Override
    public InventoryDto updateInventory(UpdateInventoryRequest request) {
        Inventory inventory = repository.findByProductId(request.productId()).orElseThrow(
                () -> new NotFoundException("Inventory not exists with productId=" + request.productId())
        );

        inventory.setQuantity(request.quantity());

        return mapper.toDto(repository.save(inventory));
    }

    @Override
    public ReserveResponse createReserve(ReserveRequest request) {
        Inventory inventory = repository.findByProductId(request.productId()).orElseThrow(
                () -> new NotFoundException("Inventory not exists with productId=" + request.productId())
        );

        if (inventory.getAvailableQuantity() < request.quantity()) {
            throw new InsufficientStockException("Available quantity = " + inventory.getAvailableQuantity() + " you not reserve " + request.quantity());
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.quantity());

        return mapper.toReserveResponse(repository.save(inventory));
    }
}
