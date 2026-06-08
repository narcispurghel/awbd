package com.github.irinabotea.webui.client;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/** REST client for animal-service (animals, shelters, species, breeds, medical records). */
@Service
public class AnimalServiceClient {

  private static final ParameterizedTypeReference<
    List<BackendDtos.AnimalDtos.AnimalSummary>
  > ANIMAL_LIST = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<
    List<BackendDtos.AnimalDtos.SpeciesView>
  > SPECIES_LIST = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<
    List<BackendDtos.AnimalDtos.ShelterView>
  > SHELTER_LIST = new ParameterizedTypeReference<>() {};

  private final BackendHttpClient http;

  public AnimalServiceClient(BackendHttpClient http) {
    this.http = http;
  }

  public List<BackendDtos.AnimalDtos.AnimalSummary> list(
    BackendDtos.AnimalDtos.@Nullable AnimalStatus status,
    @Nullable UUID speciesId,
    @Nullable UUID shelterId
  ) {
    UriComponentsBuilder b = UriComponentsBuilder.fromPath("/api/v1/animals");
    if (status != null) b.queryParam("status", status.name());
    if (speciesId != null) b.queryParam("speciesId", speciesId.toString());
    if (shelterId != null) b.queryParam("shelterId", shelterId.toString());
    return http.get(b.build().toUriString(), ANIMAL_LIST);
  }

  public List<BackendDtos.AnimalDtos.SpeciesView> species() {
    return http.get("/api/v1/species", SPECIES_LIST);
  }

  public List<BackendDtos.AnimalDtos.ShelterView> shelters() {
    return http.get("/api/v1/shelters", SHELTER_LIST);
  }
}
