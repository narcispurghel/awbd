package com.github.narcispurghel.animalservice.controller;

import com.github.narcispurghel.animalservice.dto.AnimalDtos;
import com.github.narcispurghel.animalservice.service.AnimalCatalogService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shelters")
public class ShelterController {

  private final AnimalCatalogService animalCatalogService;

  public ShelterController(AnimalCatalogService animalCatalogService) {
    this.animalCatalogService = animalCatalogService;
  }

  @GetMapping
  public List<AnimalDtos.ShelterView> list() {
    return animalCatalogService.shelters();
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public AnimalDtos.ShelterView create(@Valid @RequestBody AnimalDtos.UpsertShelterRequest body) {
    return animalCatalogService.createShelter(body);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public AnimalDtos.ShelterView update(
    @PathVariable UUID id,
    @Valid @RequestBody AnimalDtos.UpsertShelterRequest body
  ) {
    return animalCatalogService.updateShelter(id, body);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    animalCatalogService.deleteShelter(id);
  }
}
