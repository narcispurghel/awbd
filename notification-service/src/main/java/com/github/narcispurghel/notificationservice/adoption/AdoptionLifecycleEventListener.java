package com.github.narcispurghel.notificationservice.adoption;

import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionApproved;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionCancelled;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionRejected;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionSubmitted;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = AdoptionMessagingConfiguration.QUEUE_NAME)
public class AdoptionLifecycleEventListener {

  private final AdoptionNotificationDispatcher dispatcher;

  public AdoptionLifecycleEventListener(AdoptionNotificationDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @RabbitHandler
  public void handle(AdoptionSubmitted event) {
    dispatcher.dispatch(event);
  }

  @RabbitHandler
  public void handle(AdoptionApproved event) {
    dispatcher.dispatch(event);
  }

  @RabbitHandler
  public void handle(AdoptionRejected event) {
    dispatcher.dispatch(event);
  }

  @RabbitHandler
  public void handle(AdoptionCancelled event) {
    dispatcher.dispatch(event);
  }
}
