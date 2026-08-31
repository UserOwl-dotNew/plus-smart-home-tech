package ru.yandex.practicum.kafka.exception;

public class SerializationException extends RuntimeException {
    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Exception e) {
        super(message, e);
    }
}
