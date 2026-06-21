package com.github.narcispurghel.adoptionservice.controller;

import com.github.narcispurghel.adoptionservice.dto.AdoptionDtos;
import com.github.narcispurghel.adoptionservice.entity.AdoptionRequestStatus;
import com.github.narcispurghel.adoptionservice.service.AdoptionRequestService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/adoptions")
public class AdoptionRequestController {

  private final AdoptionRequestService adoptionRequestService;

  public AdoptionRequestController(AdoptionRequestService adoptionRequestService) {
    this.adoptionRequestService = adoptionRequestService;
  }

  @GetMapping
  public List<AdoptionDtos.AdoptionRequestView> list(
    Authentication authentication,
    @RequestParam(required = false) @Nullable UUID animalId,
    @RequestParam(required = false) @Nullable AdoptionRequestStatus status
  ) {
    return adoptionRequestService.list(authentication, animalId, status);
  }

  @GetMapping("/{id}")
  public AdoptionDtos.AdoptionRequestView get(Authentication authentication, @PathVariable UUID id) {
    return adoptionRequestService.get(authentication, id);
  }

  @GetMapping("/{id}/status")
  public AdoptionDtos.AdoptionStatusView status(
    Authentication authentication,
    @PathVariable UUID id
  ) {
    return adoptionRequestService.status(authentication, id);
  }

  @PostMapping
  public AdoptionDtos.AdoptionRequestView create(
    Authentication authentication,
    @Valid @RequestBody AdoptionDtos.CreateAdoptionRequest body
  ) {
    return adoptionRequestService.create(authentication, body);
  }

  @PostMapping("/{id}/review")
  @PreAuthorize("hasRole('ADMIN')")
  public AdoptionDtos.AdoptionRequestView review(
    Authentication authentication,
    @PathVariable UUID id,
    @Valid @RequestBody AdoptionDtos.ReviewAdoptionRequest body
  ) {
    return adoptionRequestService.review(authentication, id, body);
  }

  @PostMapping("/{id}/cancel")
  public AdoptionDtos.AdoptionRequestView cancel(
    Authentication authentication,
    @PathVariable UUID id
  ) {
    return adoptionRequestService.cancel(authentication, id);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(Authentication authentication, @PathVariable UUID id) {
    adoptionRequestService.delete(authentication, id);
  }
}
