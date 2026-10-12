package ru.yandex.practicum.order.exception;

public class ProductServiceUnavailableException extends RuntimeException {
    public ProductServiceUnavailableException(Object productId, Throwable cause) {
    }
}
