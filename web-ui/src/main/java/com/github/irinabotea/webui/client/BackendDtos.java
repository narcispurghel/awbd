package com.github.irinabotea.webui.client;

import java.util.List;
import org.jspecify.annotations.Nullable;

/** DTOs exchanged with user-service over REST. */
public final class BackendDtos {

    private BackendDtos() {}

    public record RegisterRequest(String email, String password, String firstName, String lastName) {}

    public record RegisterResponse(String id, String email) {}

    public record LoginRequest(String email, String password) {}

    public record LoginResponse(String token, long expiresInSeconds) {}

    public record CurrentUser(
        String id,
        String email,
        String role,
        boolean active,
        String createdAt,
        @Nullable ProfileView profile
    ) {}

    public record ProfileView(
        String firstName,
        String lastName,
        @Nullable String phone,
        @Nullable String city,
        String houseType,
        boolean hasYard,
        int experienceWithPets,
        boolean verifiedStatus
    ) {}

    public record UpdateProfileRequest(
        String firstName,
        String lastName,
        @Nullable String phone,
        @Nullable String city,
        String houseType,
        boolean hasYard,
        int experienceWithPets
    ) {}

    public record ChangePasswordRequest(String currentPassword, String newPassword) {}

    public record DeactivateRequest(String password) {}

    public record FieldError(String field, String message) {}

    public record ApiError(
        @Nullable String timestamp,
        int status,
        @Nullable String error,
        @Nullable String message,
        @Nullable List<FieldError> fieldErrors
    ) {}
}
