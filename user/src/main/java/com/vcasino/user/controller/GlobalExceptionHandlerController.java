package com.vcasino.user.controller;

import com.vcasino.exceptions.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Map<String, List<String>>>> handleConstraintViolationException(MethodArgumentNotValidException e) {
        return new ResponseEntity<>(getErrors(e), HttpStatus.BAD_REQUEST);
    }

    private Map<String, Map<String, List<String>>> getErrors(MethodArgumentNotValidException e) {
        Map<String, Map<String, List<String>>> response = new HashMap<>();
        Map<String, List<String>> errors = new HashMap<>();
        for (FieldError err : e.getBindingResult().getFieldErrors()) {
            List<String> fieldErrors = errors.getOrDefault(err.getField(), new ArrayList<>());
            fieldErrors.add(err.getDefaultMessage());
            errors.put(err.getField(), fieldErrors);
        }
        response.put("errors", errors);
        return response;
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<String> handleAppException(AppException appException) {
        return ResponseEntity.status(appException.getHttpStatus()).body(appException.getMessage());
    }
}
