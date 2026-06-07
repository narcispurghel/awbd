package com.github.narcispurghel.adoptionservice.controller;

import com.github.narcispurghel.adoptionservice.dto.ApiError;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
    List<ApiError.FieldError> fields = ex
      .getBindingResult()
      .getFieldErrors()
      .stream()
      .map(GlobalExceptionHandler::toFieldError)
      .toList();
    return ResponseEntity.badRequest().body(
      ApiError.of(400, "Bad Request", "Validation failed", fields)
    );
  }

  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<ApiError> handleStatus(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    String message = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
    return ResponseEntity.status(status).body(
      ApiError.of(status.value(), status.getReasonPhrase(), message, null)
    );
  }

  private static ApiError.FieldError toFieldError(FieldError error) {
    String message =
      error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage();
    return new ApiError.FieldError(error.getField(), message);
  }
}
