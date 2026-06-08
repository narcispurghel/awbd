package com.github.narcispurghel.notificationservice.adoption;

import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionApproved;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionCancelled;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionRejected;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionSubmitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingAdoptionNotificationAdapter implements AdoptionNotificationAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    LoggingAdoptionNotificationAdapter.class
  );

  @Override
  public void onAdoptionSubmitted(AdoptionSubmitted event) {
    LOGGER.info("Received adoption submitted event: {}", event);
  }

  @Override
  public void onAdoptionApproved(AdoptionApproved event) {
    LOGGER.info("Received adoption approved event: {}", event);
  }

  @Override
  public void onAdoptionRejected(AdoptionRejected event) {
    LOGGER.info("Received adoption rejected event: {}", event);
  }

  @Override
  public void onAdoptionCancelled(AdoptionCancelled event) {
    LOGGER.info("Received adoption cancelled event: {}", event);
  }
}
