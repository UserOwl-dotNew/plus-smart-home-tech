package ru.yandex.practicum.exception;

public class DeserializationException extends Throwable {
    public DeserializationException(String message) {
        super(message);
    }

    public DeserializationException(String m, Exception e) {
        super(m, e);
    }
}
