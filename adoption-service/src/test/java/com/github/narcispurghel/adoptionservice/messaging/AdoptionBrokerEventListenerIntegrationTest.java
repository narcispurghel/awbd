package com.github.narcispurghel.adoptionservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.narcispurghel.common.adoption.event.AdoptionLifecycleEvents;
import com.github.narcispurghel.adoptionservice.TestcontainersConfiguration;
import com.github.narcispurghel.adoptionservice.entity.AdoptionRequestStatus;
import com.github.narcispurghel.common.jwt.GatewayHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Import({TestcontainersConfiguration.class, RabbitTemplateTestConfiguration.class})
@SpringBootTest
@ActiveProfiles("test")
class AdoptionBrokerEventListenerIntegrationTest {

  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @BeforeEach
  void setUp(@Autowired WebApplicationContext context) {
    reset(rabbitTemplate);
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void createPublishesSubmittedMessage() throws Exception {
    String userId = UUID.randomUUID().toString();
    String animalId = UUID.randomUUID().toString();

    String adoptionId = createRequest(userId, animalId);

    ArgumentCaptor<AdoptionLifecycleEvents.AdoptionSubmitted> captor = ArgumentCaptor.forClass(
      AdoptionLifecycleEvents.AdoptionSubmitted.class
    );
    verify(rabbitTemplate).convertAndSend(
      eq(AdoptionBrokerEventPublisher.EXCHANGE_NAME),
      eq(AdoptionEventKeys.ADOPTION_SUBMITTED),
      captor.capture()
    );

    AdoptionLifecycleEvents.AdoptionSubmitted message = captor.getValue();
    assertThat(message.eventKey())
      .isEqualTo(AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_SUBMITTED, UUID.fromString(adoptionId)));
    assertThat(message.adoptionRequestId()).hasToString(adoptionId);
    assertThat(message.animalId()).hasToString(animalId);
    assertThat(message.adopterId()).hasToString(userId);
    assertThat(message.occurredAt()).isNotNull();
  }

  @Test
  void reviewPublishesApprovedMessageWithReviewNote() throws Exception {
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
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("APPROVED"));

    ArgumentCaptor<AdoptionLifecycleEvents.AdoptionApproved> captor = ArgumentCaptor.forClass(
      AdoptionLifecycleEvents.AdoptionApproved.class
    );
    verify(rabbitTemplate).convertAndSend(
      eq(AdoptionBrokerEventPublisher.EXCHANGE_NAME),
      eq(AdoptionEventKeys.ADOPTION_APPROVED),
      captor.capture()
    );

    AdoptionLifecycleEvents.AdoptionApproved message = captor.getValue();
    assertThat(message.eventKey())
      .isEqualTo(AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_APPROVED, UUID.fromString(adoptionId)));
    assertThat(message.adoptionRequestId()).hasToString(adoptionId);
    assertThat(message.adopterId()).hasToString(userId);
    assertThat(message.reviewedBy()).hasToString(reviewerId);
    assertThat(message.reviewNote()).isEqualTo("Looks good");
    assertThat(message.occurredAt()).isNotNull();
  }

  @Test
  void cancelPublishesCancelledMessage() throws Exception {
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
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(AdoptionRequestStatus.CANCELLED.name()));

    ArgumentCaptor<AdoptionLifecycleEvents.AdoptionCancelled> captor = ArgumentCaptor.forClass(
      AdoptionLifecycleEvents.AdoptionCancelled.class
    );
    verify(rabbitTemplate).convertAndSend(
      eq(AdoptionBrokerEventPublisher.EXCHANGE_NAME),
      eq(AdoptionEventKeys.ADOPTION_CANCELLED),
      captor.capture()
    );

    AdoptionLifecycleEvents.AdoptionCancelled message = captor.getValue();
    assertThat(message.eventKey())
      .isEqualTo(AdoptionEventKeys.eventKey(AdoptionEventKeys.ADOPTION_CANCELLED, UUID.fromString(adoptionId)));
    assertThat(message.adoptionRequestId()).hasToString(adoptionId);
    assertThat(message.animalId()).hasToString(animalId);
    assertThat(message.adopterId()).hasToString(userId);
    assertThat(message.occurredAt()).isNotNull();
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
    return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("id").asText();
  }
}
