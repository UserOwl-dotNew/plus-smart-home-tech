package ru.yandex.practicum.kafka.exception;

public class DeserializationException extends RuntimeException {
    public DeserializationException(String message) {
        super(message);
    }

    public DeserializationException(String message, Exception e) {
        super(message, e);
    }
}
