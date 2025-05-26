package ru.practicum.evmsevice.controller;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.evmsevice.dto.ApiError;
import ru.practicum.evmsevice.exception.InternalServerException;
import ru.practicum.evmsevice.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс обработки исключений при обработке поступивших http запросов
 */
@Slf4j
@RestControllerAdvice
public class ErrorAdvisor {

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

    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError onInternalException(final InternalServerException e) {
        log.error("500 {}", e.getMessage());
        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        apiError.setReason("Внутренняя ошибка сервера.");
        apiError.setMessage(e.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError onDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.error("409 {}", e.getMessage());
        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.CONFLICT);
        apiError.setReason(e.getRootCause().getMessage());
        apiError.setMessage(e.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("400 {}.", e.getMessage());
        final List<String> violations = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Field: %s. Error: %s. Value: \'%s\'. ",
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue()
                ))
                .collect(Collectors.toList());

        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.BAD_REQUEST);
        apiError.setReason("Запрос сформирован некорректно.");
        apiError.setMessage(violations.stream().collect(Collectors.joining()));
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError onConstraintValidationException(ConstraintViolationException e) {
        log.error("400 {}.", e.getMessage());
        final List<String> violations = e.getConstraintViolations().stream()
                .map(
                        violation -> String.format("Field: %s. Error: %s. Value: \'%s\'. ",
                                violation.getPropertyPath().toString(),
                                violation.getMessage(),
                                violation.getInvalidValue()
                        ))
                .collect(Collectors.toList());

        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.BAD_REQUEST);
        apiError.setReason("Запрос сформирован некорректно.");
        apiError.setMessage(violations.stream().collect(Collectors.joining()));
        apiError.setTimestamp(LocalDateTime.now());

        return apiError;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception e) {
        log.error("Error", e);
        ApiError apiError = new ApiError();
        apiError.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        apiError.setReason(e.getCause().getMessage());
        apiError.setMessage(e.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }

}
