package com.github.narcispurghel.adoptionservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionApproved;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionCancelled;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionRejected;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionSubmitted;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdoptionBrokerEventAdapterTest {

  private final AdoptionBrokerEventAdapter adapter = new AdoptionBrokerEventAdapter();

  @Test
  void adaptsSubmittedEvent() {
    Instant occurredAt = Instant.parse("2026-01-02T03:04:05Z");
    UUID adoptionRequestId = UUID.randomUUID();
    UUID animalId = UUID.randomUUID();
    UUID adopterId = UUID.randomUUID();

    AdoptionLifecycleEvents.AdoptionSubmitted message = adapter.toMessage(
      new AdoptionSubmitted(adoptionRequestId, animalId, adopterId, occurredAt)
    );

    assertThat(message)
      .isEqualTo(
        new AdoptionLifecycleEvents.AdoptionSubmitted(
          AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_SUBMITTED, adoptionRequestId),
          adoptionRequestId,
          animalId,
          adopterId,
          occurredAt
        )
      );
  }

  @Test
  void adaptsApprovedEventAndPreservesReviewNote() {
    Instant occurredAt = Instant.parse("2026-01-02T03:04:05Z");
    UUID adoptionRequestId = UUID.randomUUID();
    UUID animalId = UUID.randomUUID();
    UUID adopterId = UUID.randomUUID();
    UUID reviewedBy = UUID.randomUUID();

    AdoptionLifecycleEvents.AdoptionApproved message = adapter.toMessage(
      new AdoptionApproved(
        adoptionRequestId,
        animalId,
        adopterId,
        reviewedBy,
        "Looks good",
        occurredAt
      )
    );

    assertThat(message)
      .isEqualTo(
        new AdoptionLifecycleEvents.AdoptionApproved(
          AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_APPROVED, adoptionRequestId),
          adoptionRequestId,
          animalId,
          adopterId,
          reviewedBy,
          "Looks good",
          occurredAt
        )
      );
  }

  @Test
  void adaptsRejectedEventAndPreservesNullableReviewNote() {
    Instant occurredAt = Instant.parse("2026-01-02T03:04:05Z");
    UUID adoptionRequestId = UUID.randomUUID();
    UUID animalId = UUID.randomUUID();
    UUID adopterId = UUID.randomUUID();
    UUID reviewedBy = UUID.randomUUID();

    AdoptionLifecycleEvents.AdoptionRejected message = adapter.toMessage(
      new AdoptionRejected(adoptionRequestId, animalId, adopterId, reviewedBy, null, occurredAt)
    );

    assertThat(message.eventKey())
      .isEqualTo(AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_REJECTED, adoptionRequestId));
    assertThat(message.reviewedBy()).isEqualTo(reviewedBy);
    assertThat(message.reviewNote()).isNull();
    assertThat(message.occurredAt()).isEqualTo(occurredAt);
  }

  @Test
  void adaptsCancelledEvent() {
    Instant occurredAt = Instant.parse("2026-01-02T03:04:05Z");
    UUID adoptionRequestId = UUID.randomUUID();
    UUID animalId = UUID.randomUUID();
    UUID adopterId = UUID.randomUUID();

    AdoptionLifecycleEvents.AdoptionCancelled message = adapter.toMessage(
      new AdoptionCancelled(adoptionRequestId, animalId, adopterId, occurredAt)
    );

    assertThat(message)
      .isEqualTo(
        new AdoptionLifecycleEvents.AdoptionCancelled(
          AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_CANCELLED, adoptionRequestId),
          adoptionRequestId,
          animalId,
          adopterId,
          occurredAt
        )
      );
  }
}
