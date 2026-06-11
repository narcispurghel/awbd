package com.github.irinabotea.webui.client;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

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

  public List<BackendDtos.AdoptionDtos.AdoptionRequestView> list(
    BackendDtos.AdoptionDtos.@Nullable AdoptionRequestStatus status,
    @Nullable UUID animalId
  ) {
    UriComponentsBuilder b = UriComponentsBuilder.fromPath("/api/v1/adoptions");
    if (status != null) b.queryParam("status", status.name());
    if (animalId != null) b.queryParam("animalId", animalId.toString());
    return http.get(b.build().toUriString(), REQUEST_LIST);
  }

  public void cancel(UUID id) {
    http.postVoid("/api/v1/adoptions/" + id + "/cancel", null);
  }

  public BackendDtos.AdoptionDtos.AdoptionRequestView get(UUID id) {
    return http.get("/api/v1/adoptions/" + id, BackendDtos.AdoptionDtos.AdoptionRequestView.class);
  }

  public BackendDtos.AdoptionDtos.AdoptionRequestView review(
    UUID id,
    BackendDtos.AdoptionDtos.ReviewAdoptionRequest body
  ) {
    return http.post(
      "/api/v1/adoptions/" + id + "/review",
      body,
      BackendDtos.AdoptionDtos.AdoptionRequestView.class
    );
  }
}
