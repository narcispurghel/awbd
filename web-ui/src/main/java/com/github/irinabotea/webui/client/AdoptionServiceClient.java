package com.github.irinabotea.webui.client;

import java.util.List;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

/** REST client for adoption-service (adoption requests, status, review actions). */
@Service
public class AdoptionServiceClient {

  private static final ParameterizedTypeReference<
    List<BackendDtos.AdoptionDtos.AdoptionRequestView>
  > REQUEST_LIST = new ParameterizedTypeReference<>() {};

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

  public List<BackendDtos.AdoptionDtos.AdoptionRequestView> mine() {
    return http.get("/api/v1/adoptions", REQUEST_LIST);
  }

  public void cancel(UUID id) {
    http.postVoid("/api/v1/adoptions/" + id + "/cancel", null);
  }
}
