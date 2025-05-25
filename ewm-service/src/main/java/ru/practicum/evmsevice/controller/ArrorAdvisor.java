package ru.practicum.evmsevice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import ru.practicum.evmsevice.dto.ApiError;
import ru.practicum.evmsevice.exception.NotFoundException;
import ru.practicum.statdto.ErrorMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Класс обработки исключений при обработке поступивших http запросов
 */
@Slf4j
@RestControllerAdvice
public class ArrorAdvisor {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFoundObject(NotFoundException exception) {
        log.error("404 {}.", exception.getMessage());
        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.NOT_FOUND);
        apiError.setReason("Запрошенный объект не найден.");
        apiError.setMessage(exception.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }
}
