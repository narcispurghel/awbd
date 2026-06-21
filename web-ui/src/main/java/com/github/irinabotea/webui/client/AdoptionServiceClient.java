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
    BackendDtos.PageResponse<BackendDtos.AdoptionDtos.AdoptionRequestView>
  > REQUEST_PAGE = new ParameterizedTypeReference<>() {};

  private final BackendHttpClient http;

  public AdoptionServiceClient(BackendHttpClient http) {
    this.http = http;
  }

  public BackendDtos.AdoptionDtos.AdoptionRequestView requestAdoption(UUID animalId, @Nullable String note) {
    return http.post(
      "/api/v1/adoptions",
      new BackendDtos.AdoptionDtos.CreateAdoptionRequest(animalId, note),
      BackendDtos.AdoptionDtos.AdoptionRequestView.class
    );
  }

  public List<BackendDtos.AdoptionDtos.AdoptionRequestView> mine() {
    return minePage(0, 1000, "createdAt,desc").content();
  }

  public BackendDtos.PageResponse<BackendDtos.AdoptionDtos.AdoptionRequestView> minePage(
    int page,
    int size,
    @Nullable String sort
  ) {
    return requestPage(null, null, page, size, sort);
  }

  public List<BackendDtos.AdoptionDtos.AdoptionRequestView> list(
    BackendDtos.AdoptionDtos.@Nullable AdoptionRequestStatus status,
    @Nullable UUID animalId
  ) {
    return requestPage(status, animalId, 0, 1000, "createdAt,desc").content();
  }

  public BackendDtos.PageResponse<BackendDtos.AdoptionDtos.AdoptionRequestView> requestPage(
    BackendDtos.AdoptionDtos.@Nullable AdoptionRequestStatus status,
    @Nullable UUID animalId,
    int page,
    int size,
    @Nullable String sort
  ) {
    UriComponentsBuilder b = UriComponentsBuilder.fromPath("/api/v1/adoptions");
    if (status != null) b.queryParam("status", status.name());
    if (animalId != null) b.queryParam("animalId", animalId.toString());
    b.queryParam("page", page).queryParam("size", size);
    if (sort != null && !sort.isBlank()) b.queryParam("sort", sort);
    return http.get(b.build().toUriString(), REQUEST_PAGE);
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

  public void delete(UUID id) {
    http.delete("/api/v1/adoptions/" + id);
  }
}
