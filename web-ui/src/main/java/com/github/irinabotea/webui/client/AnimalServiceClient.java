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
    List<BackendDtos.AnimalDtos.SpeciesView>
  > SPECIES_LIST = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<
    List<BackendDtos.AnimalDtos.ShelterView>
  > SHELTER_LIST = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<
    List<BackendDtos.AnimalDtos.BreedView>
  > BREED_LIST = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<
    List<BackendDtos.AnimalDtos.TagView>
  > TAG_LIST = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<
    List<BackendDtos.AnimalDtos.AnimalPhotoView>
  > PHOTO_LIST = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<
    BackendDtos.PageResponse<BackendDtos.AnimalDtos.AnimalSummary>
  > ANIMAL_PAGE = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<
    BackendDtos.PageResponse<BackendDtos.AnimalDtos.MedicalRecordView>
  > MEDICAL_PAGE = new ParameterizedTypeReference<>() {};

  private final BackendHttpClient http;

  public AnimalServiceClient(BackendHttpClient http) {
    this.http = http;
  }

  public List<BackendDtos.AnimalDtos.AnimalSummary> list(
    BackendDtos.AnimalDtos.@Nullable AnimalStatus status,
    @Nullable UUID speciesId,
    @Nullable UUID shelterId
  ) {
    return page(status, speciesId, shelterId, 0, 1000, "name,asc").content();
  }

  public BackendDtos.PageResponse<BackendDtos.AnimalDtos.AnimalSummary> page(
    BackendDtos.AnimalDtos.@Nullable AnimalStatus status,
    @Nullable UUID speciesId,
    @Nullable UUID shelterId,
    int page,
    int size,
    @Nullable String sort
  ) {
    UriComponentsBuilder b = UriComponentsBuilder.fromPath("/api/v1/animals");
    if (status != null) b.queryParam("status", status.name());
    if (speciesId != null) b.queryParam("speciesId", speciesId.toString());
    if (shelterId != null) b.queryParam("shelterId", shelterId.toString());
    b.queryParam("page", page).queryParam("size", size);
    if (sort != null && !sort.isBlank()) b.queryParam("sort", sort);
    return http.get(b.build().toUriString(), ANIMAL_PAGE);
  }

  public BackendDtos.AnimalDtos.AnimalView get(UUID id) {
    return http.get("/api/v1/animals/" + id, BackendDtos.AnimalDtos.AnimalView.class);
  }

  public BackendDtos.AnimalDtos.AnimalView createAnimal(BackendDtos.AnimalDtos.UpsertAnimalRequest body) {
    return http.post("/api/v1/animals", body, BackendDtos.AnimalDtos.AnimalView.class);
  }

  public BackendDtos.AnimalDtos.AnimalView updateAnimal(
    UUID id,
    BackendDtos.AnimalDtos.UpsertAnimalRequest body
  ) {
    return http.put("/api/v1/animals/" + id, body, BackendDtos.AnimalDtos.AnimalView.class);
  }

  public void deleteAnimal(UUID id) {
    http.delete("/api/v1/animals/" + id);
  }

  public List<BackendDtos.AnimalDtos.MedicalRecordView> medicalRecords(UUID animalId) {
    return medicalRecordsPage(animalId, 0, 1000).content();
  }

  public BackendDtos.PageResponse<BackendDtos.AnimalDtos.MedicalRecordView> medicalRecordsPage(
    UUID animalId,
    int page,
    int size
  ) {
    UriComponentsBuilder b = UriComponentsBuilder.fromPath(
      "/api/v1/animals/" + animalId + "/medical-records"
    );
    b.queryParam("page", page).queryParam("size", size);
    return http.get(b.build().toUriString(), MEDICAL_PAGE);
  }

  public BackendDtos.AnimalDtos.MedicalRecordView addMedicalRecord(
    UUID animalId,
    BackendDtos.AnimalDtos.UpsertMedicalRecordRequest body
  ) {
    return http.post(
      "/api/v1/animals/" + animalId + "/medical-records",
      body,
      BackendDtos.AnimalDtos.MedicalRecordView.class
    );
  }

  public BackendDtos.AnimalDtos.MedicalRecordView getMedicalRecord(
    UUID animalId,
    UUID recordId
  ) {
    return http.get(
      "/api/v1/animals/" + animalId + "/medical-records/" + recordId,
      BackendDtos.AnimalDtos.MedicalRecordView.class
    );
  }

  public BackendDtos.AnimalDtos.MedicalRecordView updateMedicalRecord(
    UUID animalId,
    UUID recordId,
    BackendDtos.AnimalDtos.UpsertMedicalRecordRequest body
  ) {
    return http.put(
      "/api/v1/animals/" + animalId + "/medical-records/" + recordId,
      body,
      BackendDtos.AnimalDtos.MedicalRecordView.class
    );
  }

  public void deleteMedicalRecord(UUID animalId, UUID recordId) {
    http.delete("/api/v1/animals/" + animalId + "/medical-records/" + recordId);
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

  public void deleteShelter(UUID id) {
    http.delete("/api/v1/shelters/" + id);
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

  public void deleteSpecies(UUID id) {
    http.delete("/api/v1/species/" + id);
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

  public void deleteBreed(UUID id) {
    http.delete("/api/v1/breeds/" + id);
  }

  public List<BackendDtos.AnimalDtos.TagView> tags() {
    return http.get("/api/v1/tags", TAG_LIST);
  }

  public BackendDtos.AnimalDtos.@Nullable TagView findTag(UUID id) {
    for (BackendDtos.AnimalDtos.TagView t : tags()) {
      if (id.equals(t.id())) return t;
    }
    return null;
  }

  public BackendDtos.AnimalDtos.TagView createTag(BackendDtos.AnimalDtos.UpsertTagRequest body) {
    return http.post("/api/v1/tags", body, BackendDtos.AnimalDtos.TagView.class);
  }

  public BackendDtos.AnimalDtos.TagView updateTag(UUID id, BackendDtos.AnimalDtos.UpsertTagRequest body) {
    return http.put("/api/v1/tags/" + id, body, BackendDtos.AnimalDtos.TagView.class);
  }

  public void deleteTag(UUID id) {
    http.delete("/api/v1/tags/" + id);
  }

  public List<BackendDtos.AnimalDtos.AnimalPhotoView> photos(UUID animalId) {
    return http.get("/api/v1/animals/" + animalId + "/photos", PHOTO_LIST);
  }

  public BackendDtos.AnimalDtos.AnimalPhotoView uploadPhoto(
    UUID animalId,
    String filename,
    String contentType,
    byte[] data
  ) {
    return http.postMultipart(
      "/api/v1/animals/" + animalId + "/photos",
      "file",
      filename,
      contentType,
      data,
      BackendDtos.AnimalDtos.AnimalPhotoView.class
    );
  }

  public void deletePhoto(UUID animalId, UUID photoId) {
    http.delete("/api/v1/animals/" + animalId + "/photos/" + photoId);
  }

  public BackendHttpClient.BytesResponse photoBytes(UUID animalId, UUID photoId) {
    return http.getBytes("/api/v1/animals/" + animalId + "/photos/" + photoId);
  }
}
