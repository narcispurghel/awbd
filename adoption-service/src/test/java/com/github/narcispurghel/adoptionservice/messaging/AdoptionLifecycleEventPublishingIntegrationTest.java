package com.github.narcispurghel.adoptionservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.narcispurghel.adoptionservice.TestcontainersConfiguration;
import com.github.narcispurghel.adoptionservice.entity.AdoptionRequestStatus;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionApproved;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionCancelled;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionRejected;
import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents.AdoptionSubmitted;
import com.github.narcispurghel.common.jwt.GatewayHeaders;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Import({
  TestcontainersConfiguration.class,
  RabbitMqTestcontainersConfiguration.class
})
@SpringBootTest(properties = "notification.messaging.enabled=true")
@ActiveProfiles("test")
class AdoptionLifecycleEventPublishingIntegrationTest {

  private static final String TEST_QUEUE_NAME = "adoption.lifecycle-events.test";

  private final ObjectMapper objectMapper = new ObjectMapper();

  private MockMvc mockMvc;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @BeforeEach
  void setUp(@Autowired WebApplicationContext context) {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void publishesSubmittedEventToRabbit() throws Exception {
    String userId = UUID.randomUUID().toString();
    String animalId = UUID.randomUUID().toString();

    String adoptionId = createRequest(userId, animalId);
    AdoptionSubmitted event = awaitSubmittedEvent();

    assertThat(event.eventKey())
      .isEqualTo(AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_SUBMITTED, event.adoptionRequestId()));
    assertThat(event.adoptionRequestId().toString()).isEqualTo(adoptionId);
    assertThat(event.animalId().toString()).isEqualTo(animalId);
    assertThat(event.adopterId().toString()).isEqualTo(userId);
  }

  @Test
  void publishesApprovedEventToRabbit() throws Exception {
    String userId = UUID.randomUUID().toString();
    String animalId = UUID.randomUUID().toString();
    String adoptionId = createRequest(userId, animalId);
    String reviewerId = UUID.randomUUID().toString();

    mockMvc
      .perform(
        post("/api/v1/adoptions/{id}/review", adoptionId)
          .header(GatewayHeaders.USER_ID, reviewerId)
          .header(GatewayHeaders.USER_EMAIL, "admin@awbd.test")
          .header(GatewayHeaders.USER_ROLES, "ADMIN")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {"status":"APPROVED","reviewNote":"Looks good"}
            """
          )
      )
      .andExpect(status().isOk());

    AdoptionApproved event = awaitApprovedEvent();
    assertThat(event.eventKey())
      .isEqualTo(AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_APPROVED, event.adoptionRequestId()));
    assertThat(event.adopterId().toString()).isEqualTo(userId);
    assertThat(event.reviewedBy().toString()).isEqualTo(reviewerId);
    assertThat(event.reviewNote()).isEqualTo("Looks good");
  }

  @Test
  void publishesRejectedEventToRabbit() throws Exception {
    String userId = UUID.randomUUID().toString();
    String animalId = UUID.randomUUID().toString();
    String adoptionId = createRequest(userId, animalId);
    String reviewerId = UUID.randomUUID().toString();

    mockMvc
      .perform(
        post("/api/v1/adoptions/{id}/review", adoptionId)
          .header(GatewayHeaders.USER_ID, reviewerId)
          .header(GatewayHeaders.USER_EMAIL, "admin@awbd.test")
          .header(GatewayHeaders.USER_ROLES, "ADMIN")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {"status":"REJECTED","reviewNote":"Incomplete application"}
            """
          )
      )
      .andExpect(status().isOk());

    AdoptionRejected event = awaitRejectedEvent();
    assertThat(event.eventKey())
      .isEqualTo(AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_REJECTED, event.adoptionRequestId()));
    assertThat(event.reviewedBy().toString()).isEqualTo(reviewerId);
    assertThat(event.reviewNote()).isEqualTo("Incomplete application");
  }

  @Test
  void publishesCancelledEventToRabbit() throws Exception {
    String userId = UUID.randomUUID().toString();
    String animalId = UUID.randomUUID().toString();
    String adoptionId = createRequest(userId, animalId);

    mockMvc
      .perform(
        post("/api/v1/adoptions/{id}/cancel", adoptionId)
          .header(GatewayHeaders.USER_ID, userId)
          .header(GatewayHeaders.USER_EMAIL, "adopter@awbd.test")
          .header(GatewayHeaders.USER_ROLES, "USER")
      )
      .andExpect(status().isOk());

    AdoptionCancelled event = awaitCancelledEvent();
    assertThat(event.eventKey())
      .isEqualTo(AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_CANCELLED, event.adoptionRequestId()));
    assertThat(event.adopterId().toString()).isEqualTo(userId);
  }

  private String createRequest(String userId, String animalId) throws Exception {
    MvcResult result = mockMvc
      .perform(
        post("/api/v1/adoptions")
          .header(GatewayHeaders.USER_ID, userId)
          .header(GatewayHeaders.USER_EMAIL, "adopter@awbd.test")
          .header(GatewayHeaders.USER_ROLES, "USER")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {"animalId":"%s"}
            """.formatted(animalId)
          )
      )
      .andExpect(status().isOk())
      .andReturn();
    JsonNode json = objectMapper.readTree(result.getResponse().getContentAsByteArray());
    return json.path("id").asText();
  }

  private AdoptionSubmitted awaitSubmittedEvent() {
    return awaitMessage(AdoptionSubmitted.class);
  }

  private AdoptionApproved awaitApprovedEvent() {
    return awaitMessage(AdoptionApproved.class);
  }

  private AdoptionRejected awaitRejectedEvent() {
    return awaitMessage(AdoptionRejected.class);
  }

  private AdoptionCancelled awaitCancelledEvent() {
    return awaitMessage(AdoptionCancelled.class);
  }

  private <T> T awaitMessage(Class<T> type) {
    long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
    Object message;
    do {
      message = rabbitTemplate.receiveAndConvert(TEST_QUEUE_NAME);
      if (type.isInstance(message)) {
        return type.cast(message);
      }
      sleepQuietly();
    } while (System.nanoTime() < deadline);
    throw new AssertionError("Timed out waiting for " + type.getSimpleName());
  }

  private void sleepQuietly() {
    try {
      Thread.sleep(100L);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for broker message", ex);
    }
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class TestQueueConfiguration {

    @Bean
    Queue adoptionLifecycleTestQueue() {
      return new Queue(TEST_QUEUE_NAME, true);
    }

    @Bean
    Binding adoptionLifecycleTestBinding(
      Queue adoptionLifecycleTestQueue,
      TopicExchange adoptionLifecycleExchange
    ) {
      return BindingBuilder
        .bind(adoptionLifecycleTestQueue)
        .to(adoptionLifecycleExchange)
        .with("adoption.lifecycle.*");
    }
  }
}
