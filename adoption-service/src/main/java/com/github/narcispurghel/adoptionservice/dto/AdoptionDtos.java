package com.github.narcispurghel.adoptionservice.dto;

import com.github.narcispurghel.adoptionservice.entity.AdoptionRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public final class AdoptionDtos {

  private AdoptionDtos() {}

  public record CreateAdoptionRequest(@NotNull UUID animalId) {}

  public record ReviewAdoptionRequest(
    @NotNull AdoptionRequestStatus status,
    @Nullable @Size(max = 2000) String reviewNote
  ) {}

  public record AdoptionRequestView(
    UUID id,
    UUID animalId,
    UUID adopterId,
    AdoptionRequestStatus status,
    @Nullable UUID reviewedBy,
    @Nullable String reviewNote,
    Instant createdAt,
    Instant updatedAt
  ) {}

  public record AdoptionStatusView(UUID id, AdoptionRequestStatus status, Instant updatedAt) {}
}
