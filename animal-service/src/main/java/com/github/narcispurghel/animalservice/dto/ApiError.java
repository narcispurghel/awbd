package com.github.narcispurghel.animalservice.dto;

import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record ApiError(
  String timestamp,
  int status,
  @Nullable String error,
  @Nullable String message,
  @Nullable List<FieldError> fieldErrors
) {
  public static ApiError of(
    int status,
    String error,
    String message,
    @Nullable List<FieldError> fieldErrors
  ) {
    return new ApiError(Instant.now().toString(), status, error, message, fieldErrors);
  }

  public record FieldError(String field, String message) {}
}
