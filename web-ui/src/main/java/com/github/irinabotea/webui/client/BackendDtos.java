package com.github.irinabotea.webui.client;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** DTOs exchanged with the backend services (user, animal, adoption) over REST. */
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

  /** Mirrors the Spring Data {@code Page} JSON envelope (extra fields are ignored). */
  public record PageResponse<T>(
    List<T> content,
    int number,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last,
    int numberOfElements
  ) {}

  /** Records mirroring animal-service's {@code AnimalDtos}. */
  public static final class AnimalDtos {

    private AnimalDtos() {}

    public enum AnimalStatus {
      INTAKE,
      AVAILABLE,
      RESERVED,
      ADOPTED,
      MEDICAL_HOLD,
    }

    public enum Sex {
      FEMALE,
      MALE,
      UNKNOWN,
    }

    public record ShelterView(
      UUID id,
      String name,
      String city,
      String countryCode,
      String contactEmail,
      @Nullable String contactPhone,
      @Nullable String address
    ) {}

    public record UpsertShelterRequest(
      String name,
      String city,
      String countryCode,
      String contactEmail,
      @Nullable String contactPhone,
      @Nullable String address
    ) {}

    public record SpeciesView(UUID id, String name) {}

    public record UpsertSpeciesRequest(String name) {}

    public record BreedView(UUID id, UUID speciesId, String speciesName, String name) {}

    public record UpsertBreedRequest(UUID speciesId, String name) {}

    public record TagView(UUID id, String name) {}

    public record UpsertTagRequest(String name) {}

    public record AnimalSummary(
      UUID id,
      String name,
      AnimalStatus status,
      Sex sex,
      UUID shelterId,
      String shelterName,
      UUID speciesId,
      String speciesName,
      @Nullable UUID breedId,
      @Nullable String breedName,
      List<TagView> tags
    ) {}

    public record AnimalView(
      UUID id,
      String name,
      AnimalStatus status,
      Sex sex,
      UUID shelterId,
      String shelterName,
      UUID speciesId,
      String speciesName,
      @Nullable UUID breedId,
      @Nullable String breedName,
      @Nullable String description,
      @Nullable LocalDate birthDate,
      LocalDate intakeDate,
      @Nullable BigDecimal adoptionFee,
      boolean vaccinated,
      boolean neutered,
      List<TagView> tags
    ) {}

    public record UpsertAnimalRequest(
      String name,
      UUID shelterId,
      UUID speciesId,
      @Nullable UUID breedId,
      AnimalStatus status,
      Sex sex,
      @Nullable String description,
      @Nullable LocalDate birthDate,
      LocalDate intakeDate,
      @Nullable BigDecimal adoptionFee,
      boolean vaccinated,
      boolean neutered,
      List<UUID> tagIds
    ) {}

    public record MedicalRecordView(
      UUID id,
      UUID animalId,
      String title,
      LocalDate examinationDate,
      @Nullable String treatment,
      @Nullable String notes,
      @Nullable BigDecimal weightKg,
      boolean followUpRequired
    ) {}

    public record AnimalPhotoView(
      UUID id,
      UUID animalId,
      String contentType,
      int sortOrder,
      Instant createdAt
    ) {}

    public record UpsertMedicalRecordRequest(
      String title,
      LocalDate examinationDate,
      @Nullable String treatment,
      @Nullable String notes,
      @Nullable BigDecimal weightKg,
      boolean followUpRequired
    ) {}
  }

  /** Records mirroring adoption-service's {@code AdoptionDtos}. */
  public static final class AdoptionDtos {

    private AdoptionDtos() {}

    public enum AdoptionRequestStatus {
      PENDING,
      APPROVED,
      REJECTED,
      CANCELLED,
    }

    public record CreateAdoptionRequest(UUID animalId, @Nullable String note) {}

    public record ReviewAdoptionRequest(
      AdoptionRequestStatus status,
      @Nullable String reviewNote
    ) {}

    public record AdoptionRequestView(
      UUID id,
      UUID animalId,
      UUID adopterId,
      AdoptionRequestStatus status,
      @Nullable UUID reviewedBy,
      @Nullable String reviewNote,
      @Nullable String note,
      Instant createdAt,
      Instant updatedAt
    ) {}

    public record AdoptionStatusView(UUID id, AdoptionRequestStatus status, Instant updatedAt) {}
  }
}
