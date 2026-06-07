package com.github.narcispurghel.notificationservice.adoption;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionApproved;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionCancelled;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionRejected;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionSubmitted;
import com.github.narcispurghel.notificationservice.TestcontainersConfiguration;
import java.time.Instant;
import java.util.UUID;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.jspecify.annotations.Nullable;

@SpringBootTest(
  properties = {
    "notification.messaging.enabled=true",
    "spring.rabbitmq.listener.simple.auto-startup=true"
  }
)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AdoptionLifecycleEventListenerIntegrationTest {

  private static final String EXCHANGE_NAME = "adoption.lifecycle.events";

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @MockitoBean
  private @Nullable AdoptionNotificationAdapter adoptionNotificationAdapter;

  @BeforeEach
  void resetMock() {
    reset(adapter());
  }

  @Test
  void dispatchesSubmittedEvents() {
    AdoptionSubmitted event = new AdoptionSubmitted(
      "adoption.lifecycle.submitted:" + UUID.fromString("11111111-1111-1111-1111-111111111111"),
      UUID.fromString("11111111-1111-1111-1111-111111111111"),
      UUID.fromString("22222222-2222-2222-2222-222222222222"),
      UUID.fromString("33333333-3333-3333-3333-333333333333"),
      Instant.parse("2026-06-08T10:00:00Z")
    );

    rabbitTemplate.convertAndSend(EXCHANGE_NAME, "adoption.lifecycle.submitted", event);

    verify(adapter(), timeout(5000)).onAdoptionSubmitted(event);
  }

  @Test
  void dispatchesApprovedEvents() {
    AdoptionApproved event = new AdoptionApproved(
      "adoption.lifecycle.approved:" + UUID.fromString("44444444-4444-4444-4444-444444444444"),
      UUID.fromString("44444444-4444-4444-4444-444444444444"),
      UUID.fromString("55555555-5555-5555-5555-555555555555"),
      UUID.fromString("66666666-6666-6666-6666-666666666666"),
      UUID.fromString("77777777-7777-7777-7777-777777777777"),
      "Looks good",
      Instant.parse("2026-06-08T10:05:00Z")
    );

    rabbitTemplate.convertAndSend(EXCHANGE_NAME, "adoption.lifecycle.approved", event);

    verify(adapter(), timeout(5000)).onAdoptionApproved(event);
  }

  @Test
  void dispatchesRejectedEvents() {
    AdoptionRejected event = new AdoptionRejected(
      "adoption.lifecycle.rejected:" + UUID.fromString("88888888-8888-8888-8888-888888888888"),
      UUID.fromString("88888888-8888-8888-8888-888888888888"),
      UUID.fromString("99999999-9999-9999-9999-999999999999"),
      UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
      UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
      "Not suitable",
      Instant.parse("2026-06-08T10:10:00Z")
    );

    rabbitTemplate.convertAndSend(EXCHANGE_NAME, "adoption.lifecycle.rejected", event);

    verify(adapter(), timeout(5000)).onAdoptionRejected(event);
  }

  @Test
  void dispatchesCancelledEvents() {
    AdoptionCancelled event = new AdoptionCancelled(
      "adoption.lifecycle.cancelled:" + UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
      UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
      UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
      UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
      Instant.parse("2026-06-08T10:15:00Z")
    );

    rabbitTemplate.convertAndSend(EXCHANGE_NAME, "adoption.lifecycle.cancelled", event);

    verify(adapter(), timeout(5000)).onAdoptionCancelled(event);
  }

  private AdoptionNotificationAdapter adapter() {
    return Objects.requireNonNull(adoptionNotificationAdapter);
  }
}
