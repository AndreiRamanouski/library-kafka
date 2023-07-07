package com.library.demo.controller;

import static java.util.stream.Collectors.joining;

import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class LibraryEventsControllerAdvice {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleException(MethodArgumentNotValidException exception){

        String errorMessage = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .collect(joining(", ", "[", "]"));
        log.error("Error message is {}", errorMessage);

        return ResponseEntity.badRequest().body(errorMessage);
    }
}
