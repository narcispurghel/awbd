package com.github.narcispurghel.userservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public final class UserDtos {

  private UserDtos() {}

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
    @NotBlank @Size(max = 100) String firstName,
    @NotBlank @Size(max = 100) String lastName,
    @Nullable @Pattern(regexp = "^\\+[1-9][0-9]{7,14}$") String phone,
    @Nullable @Size(max = 100) String city,
    @NotBlank String houseType,
    boolean hasYard,
    @Min(0) int experienceWithPets
  ) {}

  public record ChangePasswordRequest(
    @NotBlank String currentPassword,
    @NotBlank @Size(min = 8, max = 100) String newPassword
  ) {}

  public record DeactivateRequest(@NotBlank String password) {}
}
