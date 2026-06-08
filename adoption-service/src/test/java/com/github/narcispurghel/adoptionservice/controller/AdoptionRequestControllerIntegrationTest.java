package com.github.narcispurghel.adoptionservice.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.narcispurghel.adoptionservice.TestcontainersConfiguration;
import com.github.narcispurghel.adoptionservice.entity.AdoptionRequestStatus;
import com.github.narcispurghel.common.jwt.GatewayHeaders;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class AdoptionRequestControllerIntegrationTest {

  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp(@Autowired WebApplicationContext context) {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void adopterCanCreateListGetStatusAndCancelRequest() throws Exception {
    String userId = UUID.randomUUID().toString();
    String animalId = UUID.randomUUID().toString();

    String adoptionId = createRequest(userId, animalId);

    mockMvc
      .perform(
        get("/api/v1/adoptions")
          .header(GatewayHeaders.USER_ID, userId)
          .header(GatewayHeaders.USER_EMAIL, "adopter@awbd.test")
          .header(GatewayHeaders.USER_ROLES, "USER")
          .param("status", AdoptionRequestStatus.PENDING.name())
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(adoptionId))
      .andExpect(jsonPath("$[0].status").value("PENDING"));

    mockMvc
      .perform(
        get("/api/v1/adoptions/{id}", adoptionId)
          .header(GatewayHeaders.USER_ID, userId)
          .header(GatewayHeaders.USER_EMAIL, "adopter@awbd.test")
          .header(GatewayHeaders.USER_ROLES, "USER")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.animalId").value(animalId))
      .andExpect(jsonPath("$.status").value("PENDING"));

    mockMvc
      .perform(
        get("/api/v1/adoptions/{id}/status", adoptionId)
          .header(GatewayHeaders.USER_ID, userId)
          .header(GatewayHeaders.USER_EMAIL, "adopter@awbd.test")
          .header(GatewayHeaders.USER_ROLES, "USER")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("PENDING"));

    mockMvc
      .perform(
        post("/api/v1/adoptions/{id}/cancel", adoptionId)
          .header(GatewayHeaders.USER_ID, userId)
          .header(GatewayHeaders.USER_EMAIL, "adopter@awbd.test")
          .header(GatewayHeaders.USER_ROLES, "USER")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("CANCELLED"));

    mockMvc
      .perform(
        get("/api/v1/adoptions/{id}/status", adoptionId)
          .header(GatewayHeaders.USER_ID, userId)
          .header(GatewayHeaders.USER_EMAIL, "adopter@awbd.test")
          .header(GatewayHeaders.USER_ROLES, "USER")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("CANCELLED"));
  }

  @Test
  void adminCanReviewRequest() throws Exception {
    String userId = UUID.randomUUID().toString();
    String animalId = UUID.randomUUID().toString();
    String adoptionId = createRequest(userId, animalId);

    mockMvc
      .perform(
        post("/api/v1/adoptions/{id}/review", adoptionId)
          .header(GatewayHeaders.USER_ID, UUID.randomUUID().toString())
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
      .andExpect(jsonPath("$.status").value("APPROVED"))
      .andExpect(jsonPath("$.reviewNote").value("Looks good"))
      .andExpect(jsonPath("$.reviewedBy").isNotEmpty());
  }

  @Test
  void unauthenticatedRequestIsRejected() throws Exception {
    mockMvc.perform(get("/api/v1/adoptions")).andExpect(status().isUnauthorized());
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
}
