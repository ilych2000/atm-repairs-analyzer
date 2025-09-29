package com.example.atmra.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * Контроллер для отображения ошибок обращения к REST сервисам.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Обрабатывает ошибки обращения к REST сервисам.
     * 
     * @param exception исключение
     * @param request запрос
     * @return результат обработки ошибки
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception exception, WebRequest request) {
        log.error("Ошибка ", exception);
        return ResponseEntity.internalServerError().body(exception.getMessage());
    }
}
