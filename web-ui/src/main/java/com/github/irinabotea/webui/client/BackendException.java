package com.github.irinabotea.webui.client;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Thrown when the backend returns a non-2xx response. Carries any field-level errors
 * so controllers can re-render forms with binding errors.
 */
public class BackendException extends RuntimeException {

    private final int status;
    private final List<BackendDtos.FieldError> fieldErrors;

    public BackendException(int status, @Nullable String message, @Nullable List<BackendDtos.FieldError> fieldErrors) {
        super(message == null ? ("Backend error " + status) : message);
        this.status = status;
        this.fieldErrors = fieldErrors == null ? List.of() : fieldErrors;
    }

    public int status() {
        return status;
    }

    public List<BackendDtos.FieldError> fieldErrors() {
        return fieldErrors;
    }

    public String safeMessage() {
        String m = getMessage();
        return m == null ? ("Backend error " + status) : m;
    }

    public boolean isUnauthorized() {
        return status == 401 || status == 403;
    }
}
