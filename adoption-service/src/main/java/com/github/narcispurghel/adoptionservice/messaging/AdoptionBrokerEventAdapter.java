package com.github.narcispurghel.adoptionservice.messaging;

import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionApproved;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionCancelled;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionRejected;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionSubmitted;
import org.springframework.stereotype.Component;

@Component
public class AdoptionBrokerEventAdapter {

  public AdoptionLifecycleEvents.AdoptionSubmitted toMessage(AdoptionSubmitted event) {
    return new AdoptionLifecycleEvents.AdoptionSubmitted(
      AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_SUBMITTED, event.adoptionRequestId()),
      event.adoptionRequestId(),
      event.animalId(),
      event.adopterId(),
      event.occurredAt()
    );
  }

  public AdoptionLifecycleEvents.AdoptionApproved toMessage(AdoptionApproved event) {
    return new AdoptionLifecycleEvents.AdoptionApproved(
      AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_APPROVED, event.adoptionRequestId()),
      event.adoptionRequestId(),
      event.animalId(),
      event.adopterId(),
      event.reviewedBy(),
      event.reviewNote(),
      event.occurredAt()
    );
  }

  public AdoptionLifecycleEvents.AdoptionRejected toMessage(AdoptionRejected event) {
    return new AdoptionLifecycleEvents.AdoptionRejected(
      AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_REJECTED, event.adoptionRequestId()),
      event.adoptionRequestId(),
      event.animalId(),
      event.adopterId(),
      event.reviewedBy(),
      event.reviewNote(),
      event.occurredAt()
    );
  }

  public AdoptionLifecycleEvents.AdoptionCancelled toMessage(AdoptionCancelled event) {
    return new AdoptionLifecycleEvents.AdoptionCancelled(
      AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_CANCELLED, event.adoptionRequestId()),
      event.adoptionRequestId(),
      event.animalId(),
      event.adopterId(),
      event.occurredAt()
    );
  }
}
