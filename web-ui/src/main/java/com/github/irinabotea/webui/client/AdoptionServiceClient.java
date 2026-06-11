package com.github.irinabotea.webui.client;

import java.util.UUID;
import org.springframework.stereotype.Service;

/** REST client for adoption-service (adoption requests, status, review actions). */
@Service
public class AdoptionServiceClient {

  private final BackendHttpClient http;

  public AdoptionServiceClient(BackendHttpClient http) {
    this.http = http;
  }

  public BackendDtos.AdoptionDtos.AdoptionRequestView requestAdoption(UUID animalId) {
    return http.post(
      "/api/v1/adoptions",
      new BackendDtos.AdoptionDtos.CreateAdoptionRequest(animalId),
      BackendDtos.AdoptionDtos.AdoptionRequestView.class
    );
  }
}
