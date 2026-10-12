package ru.yandex.practicum.order.feign.result;

public sealed interface ServiceCallResult<T> {
    record Success<T>(T data) implements ServiceCallResult<T> {}

    record Failure(String message) implements ServiceCallResult {}

    record Degraded(String message) implements ServiceCallResult {}
}
