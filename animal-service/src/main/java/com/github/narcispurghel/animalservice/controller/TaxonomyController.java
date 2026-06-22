package com.github.narcispurghel.animalservice.controller;

import com.github.narcispurghel.animalservice.dto.AnimalDtos;
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
@RequestMapping("/api/v1")
public class TaxonomyController {

  private final AnimalCatalogService animalCatalogService;

  public TaxonomyController(AnimalCatalogService animalCatalogService) {
    this.animalCatalogService = animalCatalogService;
  }

  @GetMapping("/species")
  public List<AnimalDtos.SpeciesView> species() {
    return animalCatalogService.species();
  }

  @PostMapping("/species")
  @PreAuthorize("hasRole('ADMIN')")
  public AnimalDtos.SpeciesView createSpecies(
    @Valid @RequestBody AnimalDtos.UpsertSpeciesRequest body
  ) {
    return animalCatalogService.createSpecies(body);
  }

  @PutMapping("/species/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public AnimalDtos.SpeciesView updateSpecies(
    @PathVariable UUID id,
    @Valid @RequestBody AnimalDtos.UpsertSpeciesRequest body
  ) {
    return animalCatalogService.updateSpecies(id, body);
  }

  @DeleteMapping("/species/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSpecies(@PathVariable UUID id) {
    animalCatalogService.deleteSpecies(id);
  }

  @GetMapping("/breeds")
  public List<AnimalDtos.BreedView> breeds(@RequestParam(required = false) @Nullable UUID speciesId) {
    return animalCatalogService.breeds(speciesId);
  }

  @PostMapping("/breeds")
  @PreAuthorize("hasRole('ADMIN')")
  public AnimalDtos.BreedView createBreed(@Valid @RequestBody AnimalDtos.UpsertBreedRequest body) {
    return animalCatalogService.createBreed(body);
  }

  @PutMapping("/breeds/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public AnimalDtos.BreedView updateBreed(
    @PathVariable UUID id,
    @Valid @RequestBody AnimalDtos.UpsertBreedRequest body
  ) {
    return animalCatalogService.updateBreed(id, body);
  }

  @DeleteMapping("/breeds/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBreed(@PathVariable UUID id) {
    animalCatalogService.deleteBreed(id);
  }

  @GetMapping("/tags")
  public List<AnimalDtos.TagView> tags() {
    return animalCatalogService.tags();
  }

  @PostMapping("/tags")
  @PreAuthorize("hasRole('ADMIN')")
  public AnimalDtos.TagView createTag(@Valid @RequestBody AnimalDtos.UpsertTagRequest body) {
    return animalCatalogService.createTag(body);
  }

  @PutMapping("/tags/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public AnimalDtos.TagView updateTag(
    @PathVariable UUID id,
    @Valid @RequestBody AnimalDtos.UpsertTagRequest body
  ) {
    return animalCatalogService.updateTag(id, body);
  }

  @DeleteMapping("/tags/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTag(@PathVariable UUID id) {
    animalCatalogService.deleteTag(id);
  }
}
