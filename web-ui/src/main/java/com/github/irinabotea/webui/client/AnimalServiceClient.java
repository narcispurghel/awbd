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
  private static final ParameterizedTypeReference<
    List<BackendDtos.AnimalDtos.BreedView>
  > BREED_LIST = new ParameterizedTypeReference<>() {};

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

  public BackendDtos.AnimalDtos.AnimalView get(UUID id) {
    return http.get("/api/v1/animals/" + id, BackendDtos.AnimalDtos.AnimalView.class);
  }

  public BackendDtos.AnimalDtos.AnimalView createAnimal(BackendDtos.AnimalDtos.UpsertAnimalRequest body) {
    return http.post("/api/v1/animals", body, BackendDtos.AnimalDtos.AnimalView.class);
  }

  public List<BackendDtos.AnimalDtos.SpeciesView> species() {
    return http.get("/api/v1/species", SPECIES_LIST);
  }

  public List<BackendDtos.AnimalDtos.ShelterView> shelters() {
    return http.get("/api/v1/shelters", SHELTER_LIST);
  }

  public BackendDtos.AnimalDtos.@Nullable ShelterView findShelter(UUID id) {
    for (BackendDtos.AnimalDtos.ShelterView s : shelters()) {
      if (id.equals(s.id())) return s;
    }
    return null;
  }

  public BackendDtos.AnimalDtos.ShelterView createShelter(BackendDtos.AnimalDtos.UpsertShelterRequest body) {
    return http.post("/api/v1/shelters", body, BackendDtos.AnimalDtos.ShelterView.class);
  }

  public BackendDtos.AnimalDtos.ShelterView updateShelter(UUID id, BackendDtos.AnimalDtos.UpsertShelterRequest body) {
    return http.put("/api/v1/shelters/" + id, body, BackendDtos.AnimalDtos.ShelterView.class);
  }

  public BackendDtos.AnimalDtos.@Nullable SpeciesView findSpecies(UUID id) {
    for (BackendDtos.AnimalDtos.SpeciesView s : species()) {
      if (id.equals(s.id())) return s;
    }
    return null;
  }

  public BackendDtos.AnimalDtos.SpeciesView createSpecies(BackendDtos.AnimalDtos.UpsertSpeciesRequest body) {
    return http.post("/api/v1/species", body, BackendDtos.AnimalDtos.SpeciesView.class);
  }

  public BackendDtos.AnimalDtos.SpeciesView updateSpecies(UUID id, BackendDtos.AnimalDtos.UpsertSpeciesRequest body) {
    return http.put("/api/v1/species/" + id, body, BackendDtos.AnimalDtos.SpeciesView.class);
  }

  public List<BackendDtos.AnimalDtos.BreedView> breeds(@Nullable UUID speciesId) {
    UriComponentsBuilder b = UriComponentsBuilder.fromPath("/api/v1/breeds");
    if (speciesId != null) b.queryParam("speciesId", speciesId.toString());
    return http.get(b.build().toUriString(), BREED_LIST);
  }

  public BackendDtos.AnimalDtos.@Nullable BreedView findBreed(UUID id) {
    for (BackendDtos.AnimalDtos.BreedView b : breeds(null)) {
      if (id.equals(b.id())) return b;
    }
    return null;
  }

  public BackendDtos.AnimalDtos.BreedView createBreed(BackendDtos.AnimalDtos.UpsertBreedRequest body) {
    return http.post("/api/v1/breeds", body, BackendDtos.AnimalDtos.BreedView.class);
  }

  public BackendDtos.AnimalDtos.BreedView updateBreed(UUID id, BackendDtos.AnimalDtos.UpsertBreedRequest body) {
    return http.put("/api/v1/breeds/" + id, body, BackendDtos.AnimalDtos.BreedView.class);
  }
}
