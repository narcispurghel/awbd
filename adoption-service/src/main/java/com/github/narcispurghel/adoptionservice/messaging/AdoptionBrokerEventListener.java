package com.github.narcispurghel.adoptionservice.messaging;

import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionApproved;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionCancelled;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionRejected;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionSubmitted;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AdoptionBrokerEventListener {

  private final AdoptionBrokerEventAdapter eventAdapter;
  private final AdoptionBrokerEventPublisher eventPublisher;

  public AdoptionBrokerEventListener(
    AdoptionBrokerEventAdapter eventAdapter,
    AdoptionBrokerEventPublisher eventPublisher
  ) {
    this.eventAdapter = eventAdapter;
    this.eventPublisher = eventPublisher;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onAdoptionSubmitted(AdoptionSubmitted event) {
    eventPublisher.publish(
      eventAdapter.toMessage(event),
      AdoptionEventKeys.ADOPTION_SUBMITTED
    );
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onAdoptionApproved(AdoptionApproved event) {
    eventPublisher.publish(
      eventAdapter.toMessage(event),
      AdoptionEventKeys.ADOPTION_APPROVED
    );
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onAdoptionRejected(AdoptionRejected event) {
    eventPublisher.publish(
      eventAdapter.toMessage(event),
      AdoptionEventKeys.ADOPTION_REJECTED
    );
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onAdoptionCancelled(AdoptionCancelled event) {
    eventPublisher.publish(
      eventAdapter.toMessage(event),
      AdoptionEventKeys.ADOPTION_CANCELLED
    );
  }
}
