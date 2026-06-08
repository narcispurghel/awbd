package com.github.narcispurghel.adoptionservice.messaging;

import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AdoptionBrokerEventPublisher {

  public static final String EXCHANGE_NAME = "adoption.lifecycle.events";

  private final RabbitTemplate rabbitTemplate;

  public AdoptionBrokerEventPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publish(AdoptionLifecycleEvents.AdoptionSubmitted message, String routingKey) {
    rabbitTemplate.convertAndSend(EXCHANGE_NAME, routingKey, message);
  }

  public void publish(AdoptionLifecycleEvents.AdoptionApproved message, String routingKey) {
    rabbitTemplate.convertAndSend(EXCHANGE_NAME, routingKey, message);
  }

  public void publish(AdoptionLifecycleEvents.AdoptionRejected message, String routingKey) {
    rabbitTemplate.convertAndSend(EXCHANGE_NAME, routingKey, message);
  }

  public void publish(AdoptionLifecycleEvents.AdoptionCancelled message, String routingKey) {
    rabbitTemplate.convertAndSend(EXCHANGE_NAME, routingKey, message);
  }
}
