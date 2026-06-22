package com.github.narcispurghel.animalservice.dto;

import com.github.narcispurghel.animalservice.entity.AnimalStatus;
import com.github.narcispurghel.animalservice.entity.Sex;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public final class AnimalDtos {

  private AnimalDtos() {}

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
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Size(min = 2, max = 2) String countryCode,
    @NotBlank @Size(max = 255) String contactEmail,
    @Nullable @Size(max = 30) String contactPhone,
    @Nullable @Size(max = 255) String address
  ) {}

  public record SpeciesView(UUID id, String name) {}

  public record UpsertSpeciesRequest(@NotBlank @Size(max = 80) String name) {}

  public record BreedView(UUID id, UUID speciesId, String speciesName, String name) {}

  public record UpsertBreedRequest(
    @NotNull UUID speciesId,
    @NotBlank @Size(max = 80) String name
  ) {}

  public record TagView(UUID id, String name) {}

  public record UpsertTagRequest(@NotBlank @Size(max = 80) String name) {}

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
    @NotBlank @Size(max = 120) String name,
    @NotNull UUID shelterId,
    @NotNull UUID speciesId,
    @Nullable UUID breedId,
    @NotNull AnimalStatus status,
    @NotNull Sex sex,
    @Nullable @Size(max = 2000) String description,
    @Nullable LocalDate birthDate,
    @NotNull LocalDate intakeDate,
    @Nullable @DecimalMin("0.0") BigDecimal adoptionFee,
    boolean vaccinated,
    boolean neutered,
    @Nullable List<UUID> tagIds
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

  public record UpsertMedicalRecordRequest(
    @NotBlank @Size(max = 120) String title,
    @NotNull LocalDate examinationDate,
    @Nullable @Size(max = 255) String treatment,
    @Nullable @Size(max = 2000) String notes,
    @Nullable @DecimalMin("0.0") BigDecimal weightKg,
    boolean followUpRequired
  ) {}

  public record AnimalPhotoView(
    UUID id,
    UUID animalId,
    String contentType,
    int sortOrder,
    Instant createdAt
  ) {}
}
