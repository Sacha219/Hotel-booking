package com.example.hotelbooking.exception;

import com.example.hotelbooking.dto.ErrorDto;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Hidden
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGenericException(Exception exception) {
        log.error("Произошла ошибка: {}", exception.getMessage(), exception);
        ErrorDto error = new ErrorDto(
                "Ошибка сервера",
                "Произошла внутренняя ошибка сервера. Пожалуйста, попробуйте позже.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEntityNotFoundException(EntityNotFoundException exception) {
        log.error("Сущность не найдена: {}", exception.getMessage());
        ErrorDto error = new ErrorDto(
                "Не найдено",
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorDto> handleNoSuchElementException(NoSuchElementException exception) {
        log.error("Элемент не найден: {}", exception.getMessage());
        ErrorDto error = new ErrorDto(
                "Не найдено",
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationException(MethodArgumentNotValidException exception) {
        String errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.error("Ошибка валидации: {}", errors);
        ErrorDto error = new ErrorDto(
                "Ошибка валидации",
                "Проверьте правильность заполнения полей: " + errors,
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleBadRequestException(HttpMessageNotReadableException exception) {
        log.error("Ошибка чтения JSON: {}", exception.getMessage());

        String userMessage = "Неверный формат запроса";

        if (exception.getCause() instanceof DateTimeParseException) {
            userMessage = "Некорректная дата. Пожалуйста, используйте формат ГГГГ-ММ-ДД и проверьте, что дата существует (например, 31 февраля не существует)";
        } else if (exception.getMessage().contains("LocalDate")) {
            userMessage = "Неверный формат даты. Ожидается формат: ГГГГ-ММ-ДД (например, 2026-05-10)";
        }

        ErrorDto error = new ErrorDto(
                "Неверный запрос",
                userMessage,
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.error("Неверный аргумент: {}", exception.getMessage());
        ErrorDto error = new ErrorDto(
                "Неверный запрос",
                exception.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDto> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.error("Ошибка целостности данных: {}", exception.getMessage());

        String userMessage = "Нарушение уникальности данных";

        if (exception.getMessage().contains("email")) {
            userMessage = "Гость с таким email уже существует. Пожалуйста, используйте другой email.";
        } else if (exception.getMessage().contains("name")) {
            userMessage = "Запись с таким названием уже существует.";
        }

        ErrorDto error = new ErrorDto(
                "Конфликт данных",
                userMessage,
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}