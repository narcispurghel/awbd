package com.github.narcispurghel.userservice.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.narcispurghel.common.jwt.GatewayHeaders;
import com.github.narcispurghel.userservice.TestcontainersConfiguration;
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

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class UserControllerIntegrationTest {

  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private String userId;

  @BeforeEach
  void setUp(@Autowired WebApplicationContext context) throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    registerUser();
  }

  private void registerUser() throws Exception {
    String registerBody = """
      {"email":"me@example.com","password":"password123","firstName":"Me","lastName":"User"}
      """;
    MvcResult result = mockMvc
      .perform(
        post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody)
      )
      .andExpect(status().isOk())
      .andReturn();
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    userId = body.get("id").asText();
  }

  @Test
  void currentUserWithGatewayHeaders() throws Exception {
    mockMvc
      .perform(
        get("/api/v1/users/me")
          .header(GatewayHeaders.USER_ID, userId)
          .header(GatewayHeaders.USER_EMAIL, "me@example.com")
          .header(GatewayHeaders.USER_ROLES, "USER")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.email").value("me@example.com"))
      .andExpect(jsonPath("$.profile.firstName").value("Me"));
  }
}
