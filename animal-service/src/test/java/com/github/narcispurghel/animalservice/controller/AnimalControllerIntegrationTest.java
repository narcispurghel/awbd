package com.github.narcispurghel.animalservice.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.narcispurghel.animalservice.TestcontainersConfiguration;
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
class AnimalControllerIntegrationTest {

  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp(@Autowired WebApplicationContext context) {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void adminCanCreateAnimalCatalogFlow() throws Exception {
    String shelterId = create(
      "/api/v1/shelters",
      """
      {
        "name": "Safe Paws",
        "city": "Cluj-Napoca",
        "countryCode": "RO",
        "contactEmail": "contact@safepaws.test",
        "contactPhone": "+40111222333",
        "address": "12 Oak Street"
      }
      """
    );

    String speciesId = create(
      "/api/v1/species",
      """
      {
        "name": "Dog"
      }
      """
    );

    String breedId = create(
      "/api/v1/breeds",
      """
      {
        "speciesId": "%s",
        "name": "Labrador Retriever"
      }
      """.formatted(speciesId)
    );

    String tagId = create(
      "/api/v1/tags",
      """
      {
        "name": "Good with kids"
      }
      """
    );

    String animalId = create(
      "/api/v1/animals",
      """
      {
        "name": "Milo",
        "shelterId": "%s",
        "speciesId": "%s",
        "breedId": "%s",
        "status": "AVAILABLE",
        "sex": "MALE",
        "description": "Friendly and energetic",
        "birthDate": "2023-03-01",
        "intakeDate": "2025-01-10",
        "adoptionFee": 120.00,
        "vaccinated": true,
        "neutered": true,
        "tagIds": ["%s"]
      }
      """.formatted(shelterId, speciesId, breedId, tagId)
    );

    mockMvc
      .perform(get("/api/v1/animals").headers(userHeaders()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(animalId))
      .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"));

    mockMvc
      .perform(get("/api/v1/animals/{id}", animalId).headers(userHeaders()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value("Milo"))
      .andExpect(jsonPath("$.breedName").value("Labrador Retriever"))
      .andExpect(jsonPath("$.tags[0].name").value("Good with kids"));

    mockMvc
      .perform(
        post("/api/v1/animals/{id}/medical-records", animalId)
          .headers(adminHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "title": "Initial exam",
              "examinationDate": "2025-01-11",
              "treatment": "Vaccination review",
              "notes": "Healthy overall",
              "weightKg": 18.40,
              "followUpRequired": false
            }
            """
          )
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.animalId").value(animalId));

    mockMvc
      .perform(get("/api/v1/animals/{id}/medical-records", animalId).headers(userHeaders()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].title").value("Initial exam"));
  }

  @Test
  void nonAdminCannotCreateSpecies() throws Exception {
    mockMvc
      .perform(
        post("/api/v1/species")
          .headers(userHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "name": "Cat"
            }
            """
          )
      )
      .andExpect(status().isForbidden());
  }

  @Test
  void unauthenticatedWriteRequestIsRejected() throws Exception {
    mockMvc
      .perform(
        post("/api/v1/tags")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            """
            {
              "name": "Quiet"
            }
            """
          )
      )
      .andExpect(status().isUnauthorized());
  }

  private String create(String path, String body) throws Exception {
    MvcResult result = mockMvc
      .perform(post(path).headers(adminHeaders()).contentType(MediaType.APPLICATION_JSON).content(body))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode json = objectMapper.readTree(result.getResponse().getContentAsByteArray());
    return json.get("id").asText();
  }

  private org.springframework.http.HttpHeaders adminHeaders() {
    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.add(GatewayHeaders.USER_ID, UUID.randomUUID().toString());
    headers.add(GatewayHeaders.USER_EMAIL, "admin@awbd.test");
    headers.add(GatewayHeaders.USER_ROLES, "ADMIN");
    return headers;
  }

  private org.springframework.http.HttpHeaders userHeaders() {
    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.add(GatewayHeaders.USER_ID, UUID.randomUUID().toString());
    headers.add(GatewayHeaders.USER_EMAIL, "user@awbd.test");
    headers.add(GatewayHeaders.USER_ROLES, "USER");
    return headers;
  }
}
