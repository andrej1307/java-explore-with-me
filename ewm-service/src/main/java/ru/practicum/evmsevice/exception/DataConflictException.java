package ru.practicum.evmsevice.exception;

public class DataConflictException extends RuntimeException {
    public DataConflictException(String message) {
        super(message);
    }
}