package com.github.narcispurghel.animalservice.controller;

import com.github.narcispurghel.animalservice.dto.AnimalDtos;
import com.github.narcispurghel.animalservice.entity.AnimalStatus;
import com.github.narcispurghel.animalservice.service.AnimalCatalogService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/animals")
public class AnimalController {

  private final AnimalCatalogService animalCatalogService;

  public AnimalController(AnimalCatalogService animalCatalogService) {
    this.animalCatalogService = animalCatalogService;
  }

  @GetMapping
  public List<AnimalDtos.AnimalSummary> list(
    @RequestParam(required = false) @Nullable AnimalStatus status,
    @RequestParam(required = false) @Nullable UUID speciesId,
    @RequestParam(required = false) @Nullable UUID shelterId
  ) {
    return animalCatalogService.animals(status, speciesId, shelterId);
  }

  @GetMapping("/{id}")
  public AnimalDtos.AnimalView get(@PathVariable UUID id) {
    return animalCatalogService.animal(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public AnimalDtos.AnimalView create(@Valid @RequestBody AnimalDtos.UpsertAnimalRequest body) {
    return animalCatalogService.createAnimal(body);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public AnimalDtos.AnimalView update(
    @PathVariable UUID id,
    @Valid @RequestBody AnimalDtos.UpsertAnimalRequest body
  ) {
    return animalCatalogService.updateAnimal(id, body);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    animalCatalogService.deleteAnimal(id);
  }

  @GetMapping("/{id}/medical-records")
  public List<AnimalDtos.MedicalRecordView> medicalRecords(@PathVariable UUID id) {
    return animalCatalogService.medicalRecords(id);
  }

  @PostMapping("/{id}/medical-records")
  @PreAuthorize("hasRole('ADMIN')")
  public AnimalDtos.MedicalRecordView addMedicalRecord(
    @PathVariable UUID id,
    @Valid @RequestBody AnimalDtos.UpsertMedicalRecordRequest body
  ) {
    return animalCatalogService.addMedicalRecord(id, body);
  }

  @PutMapping("/{animalId}/medical-records/{recordId}")
  @PreAuthorize("hasRole('ADMIN')")
  public AnimalDtos.MedicalRecordView updateMedicalRecord(
    @PathVariable UUID animalId,
    @PathVariable UUID recordId,
    @Valid @RequestBody AnimalDtos.UpsertMedicalRecordRequest body
  ) {
    return animalCatalogService.updateMedicalRecord(animalId, recordId, body);
  }

  @DeleteMapping("/{animalId}/medical-records/{recordId}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteMedicalRecord(@PathVariable UUID animalId, @PathVariable UUID recordId) {
    animalCatalogService.deleteMedicalRecord(animalId, recordId);
  }
}
