package com.github.narcispurghel.userservice.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.narcispurghel.userservice.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp(@Autowired WebApplicationContext context) {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void registerAndLogin() throws Exception {
    String registerBody = """
      {"email":"user@example.com","password":"password123","firstName":"Ana","lastName":"Pop"}
      """;
    mockMvc
      .perform(
        post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.email").value("user@example.com"));

    String loginBody = """
      {"email":"user@example.com","password":"password123"}
      """;
    mockMvc
      .perform(
        post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.token").isNotEmpty())
      .andExpect(jsonPath("$.expiresInSeconds").isNumber());
  }
}
