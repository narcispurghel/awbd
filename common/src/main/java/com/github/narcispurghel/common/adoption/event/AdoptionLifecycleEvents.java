package com.github.narcispurghel.common.adoption.event;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public final class AdoptionLifecycleEvents {

  private AdoptionLifecycleEvents() {}

  public record AdoptionSubmitted(
    String eventKey,
    UUID adoptionRequestId,
    UUID animalId,
    UUID adopterId,
    Instant occurredAt
  ) {}

  public record AdoptionApproved(
    String eventKey,
    UUID adoptionRequestId,
    UUID animalId,
    UUID adopterId,
    UUID reviewedBy,
    @Nullable String reviewNote,
    Instant occurredAt
  ) {}

  public record AdoptionRejected(
    String eventKey,
    UUID adoptionRequestId,
    UUID animalId,
    UUID adopterId,
    UUID reviewedBy,
    @Nullable String reviewNote,
    Instant occurredAt
  ) {}

  public record AdoptionCancelled(
    String eventKey,
    UUID adoptionRequestId,
    UUID animalId,
    UUID adopterId,
    Instant occurredAt
  ) {}
}
