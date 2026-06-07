package com.github.narcispurghel.adoptionservice.event;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public final class AdoptionEvents {

  private AdoptionEvents() {}

  public record AdoptionSubmitted(
    UUID adoptionRequestId,
    UUID animalId,
    UUID adopterId,
    Instant occurredAt
  ) {}

  public record AdoptionApproved(
    UUID adoptionRequestId,
    UUID animalId,
    UUID adopterId,
    UUID reviewedBy,
    @Nullable String reviewNote,
    Instant occurredAt
  ) {}

  public record AdoptionRejected(
    UUID adoptionRequestId,
    UUID animalId,
    UUID adopterId,
    UUID reviewedBy,
    @Nullable String reviewNote,
    Instant occurredAt
  ) {}

  public record AdoptionCancelled(
    UUID adoptionRequestId,
    UUID animalId,
    UUID adopterId,
    Instant occurredAt
  ) {}
}
