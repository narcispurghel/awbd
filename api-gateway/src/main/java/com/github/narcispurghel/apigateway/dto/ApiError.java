package com.github.narcispurghel.apigateway.dto;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record ApiError(
  String timestamp,
  int status,
  @Nullable String error,
  @Nullable String message,
  @Nullable Object fieldErrors
) {
  public static ApiError of(
    int status,
    String error,
    String message,
    @Nullable Object fieldErrors
  ) {
    return new ApiError(Instant.now().toString(), status, error, message, fieldErrors);
  }
}
