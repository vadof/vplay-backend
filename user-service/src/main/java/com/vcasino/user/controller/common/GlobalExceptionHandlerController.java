package com.vcasino.user.controller.common;

import com.vcasino.user.exception.AppException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Exception handler", description = "Handles application exceptions")
@RestControllerAdvice
public class GlobalExceptionHandlerController {

    @Operation(summary = "Field validation exception")
    @ApiResponse(responseCode = "400", description = "Invalid fields",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FieldExceptionResponse.class)))
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FieldExceptionResponse> handleConstraintViolationException(MethodArgumentNotValidException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new FieldExceptionResponse("Invalid Fields", getErrors(e)));
    }

    private Map<String, List<String>> getErrors(MethodArgumentNotValidException e) {
        Map<String, List<String>> errors = new HashMap<>();
        for (FieldError err : e.getBindingResult().getFieldErrors()) {
            List<String> fieldErrors = errors.getOrDefault(err.getField(), new ArrayList<>());
            fieldErrors.add(err.getDefaultMessage());
            errors.put(err.getField(), fieldErrors);
        }

        return errors;
    }

    @Operation(summary = "Common exception")
    @ApiResponse(responseCode = "400+", description = "App exception", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ExceptionMessage.class)))
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ExceptionMessage> handleAppException(AppException appException) {
        return ResponseEntity.status(appException.getHttpStatus())
                .body(new ExceptionMessage(appException.getMessage()));
    }

    @Getter
    public static class FieldExceptionResponse {
        String message;
        Map<String, List<String>> errors;

        FieldExceptionResponse(String message, Map<String, List<String>> errors) {
            this.message = message;
            this.errors = errors;
        }
    }

    @Getter
    public static class ExceptionMessage {
        String message;

        ExceptionMessage(String message) {
            this.message = message;
        }
    }
}
