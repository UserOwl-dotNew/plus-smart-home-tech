package ru.yandex.practicum.exception;

public class SerializationException extends RuntimeException {
    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String m, Exception e) {
        super(m, e);
    }
}
