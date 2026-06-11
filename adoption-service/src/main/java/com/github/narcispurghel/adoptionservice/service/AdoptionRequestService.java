package com.github.narcispurghel.adoptionservice.service;

import com.github.narcispurghel.adoptionservice.dto.AdoptionDtos;
import com.github.narcispurghel.adoptionservice.entity.AdoptionRequest;
import com.github.narcispurghel.adoptionservice.entity.AdoptionRequestStatus;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionApproved;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionCancelled;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionRejected;
import com.github.narcispurghel.adoptionservice.event.AdoptionEvents.AdoptionSubmitted;
import com.github.narcispurghel.adoptionservice.repository.AdoptionRequestJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AdoptionRequestService {

  private final AdoptionRequestJpaRepository adoptionRequestRepository;
  private final ApplicationEventPublisher eventPublisher;

  public AdoptionRequestService(
    AdoptionRequestJpaRepository adoptionRequestRepository,
    ApplicationEventPublisher eventPublisher
  ) {
    this.adoptionRequestRepository = adoptionRequestRepository;
    this.eventPublisher = eventPublisher;
  }

  @Transactional(readOnly = true)
  public List<AdoptionDtos.AdoptionRequestView> list(
    Authentication authentication,
    @Nullable UUID animalId,
    @Nullable AdoptionRequestStatus status
  ) {
    Specification<AdoptionRequest> specification = byOptionalFilters(animalId, status);
    if (!isAdmin(authentication)) {
      specification = specification.and(adopterIdEquals(currentUserId(authentication)));
    }
    return adoptionRequestRepository
      .findAll(specification)
      .stream()
      .map(this::toView)
      .toList();
  }

  @Transactional(readOnly = true)
  public AdoptionDtos.AdoptionRequestView get(Authentication authentication, UUID id) {
    return toView(requireVisibleRequest(authentication, id));
  }

  @Transactional(readOnly = true)
  public AdoptionDtos.AdoptionStatusView status(Authentication authentication, UUID id) {
    AdoptionRequest request = requireVisibleRequest(authentication, id);
    return new AdoptionDtos.AdoptionStatusView(
      request.getId(),
      request.getStatus(),
      request.getUpdatedAt()
    );
  }

  public AdoptionDtos.AdoptionRequestView create(
    Authentication authentication,
    AdoptionDtos.CreateAdoptionRequest body
  ) {
    AdoptionRequest request = new AdoptionRequest();
    request.setAnimalId(body.animalId());
    request.setAdopterId(currentUserId(authentication));
    request.setStatus(AdoptionRequestStatus.PENDING);
    request.setReviewedBy(null);
    request.setReviewNote(null);
    request.setNote(normalizeNote(body.note()));
    AdoptionRequest saved = adoptionRequestRepository.save(request);
    eventPublisher.publishEvent(
      new AdoptionSubmitted(
        saved.getId(),
        saved.getAnimalId(),
        saved.getAdopterId(),
        Instant.now()
      )
    );
    return toView(saved);
  }

  public AdoptionDtos.AdoptionRequestView review(
    Authentication authentication,
    UUID id,
    AdoptionDtos.ReviewAdoptionRequest body
  ) {
    requireAdmin(authentication);
    UUID reviewedBy = currentUserId(authentication);
    AdoptionRequestStatus status = body.status();
    if (status != AdoptionRequestStatus.APPROVED && status != AdoptionRequestStatus.REJECTED) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review must approve or reject");
    }
    AdoptionRequest request = requireRequest(id);
    request.setStatus(status);
    request.setReviewedBy(reviewedBy);
    request.setReviewNote(normalizeReviewNote(body.reviewNote()));
    request.touch();
    AdoptionRequest saved = adoptionRequestRepository.save(request);
    if (status == AdoptionRequestStatus.APPROVED) {
      eventPublisher.publishEvent(
        new AdoptionApproved(
          saved.getId(),
          saved.getAnimalId(),
          saved.getAdopterId(),
          reviewedBy,
          saved.getReviewNote(),
          Instant.now()
        )
      );
    } else {
      eventPublisher.publishEvent(
        new AdoptionRejected(
          saved.getId(),
          saved.getAnimalId(),
          saved.getAdopterId(),
          reviewedBy,
          saved.getReviewNote(),
          Instant.now()
        )
      );
    }
    return toView(saved);
  }

  public AdoptionDtos.AdoptionRequestView cancel(Authentication authentication, UUID id) {
    AdoptionRequest request = requireVisibleRequest(authentication, id);
    if (request.getStatus() != AdoptionRequestStatus.PENDING) {
      throw new ResponseStatusException(
        HttpStatus.CONFLICT,
        "Only pending adoption requests can be cancelled"
      );
    }
    request.setStatus(AdoptionRequestStatus.CANCELLED);
    request.touch();
    AdoptionRequest saved = adoptionRequestRepository.save(request);
    eventPublisher.publishEvent(
      new AdoptionCancelled(
        saved.getId(),
        saved.getAnimalId(),
        saved.getAdopterId(),
        Instant.now()
      )
    );
    return toView(saved);
  }

  private Specification<AdoptionRequest> byOptionalFilters(
    @Nullable UUID animalId,
    @Nullable AdoptionRequestStatus status
  ) {
    Specification<AdoptionRequest> specification = (root, _, cb) -> cb.conjunction();
    if (animalId != null) {
      specification = specification.and((root, _, cb) -> cb.equal(root.get("animalId"), animalId));
    }
    if (status != null) {
      specification = specification.and((root, _, cb) -> cb.equal(root.get("status"), status));
    }
    return specification;
  }

  private Specification<AdoptionRequest> adopterIdEquals(UUID adopterId) {
    return (root, _, cb) -> cb.equal(root.get("adopterId"), adopterId);
  }

  private AdoptionRequest requireVisibleRequest(Authentication authentication, UUID id) {
    AdoptionRequest request = requireRequest(id);
    if (isAdmin(authentication)) {
      return request;
    }
    if (!request.getAdopterId().equals(currentUserId(authentication))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Adoption request not accessible");
    }
    return request;
  }

  private AdoptionRequest requireRequest(UUID id) {
    return adoptionRequestRepository
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
  }

  private void requireAdmin(Authentication authentication) {
    if (!isAdmin(authentication)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
    }
  }

  private boolean isAdmin(Authentication authentication) {
    return authentication
      .getAuthorities()
      .stream()
      .map(GrantedAuthority::getAuthority)
      .anyMatch("ROLE_ADMIN"::equals);
  }

  private UUID currentUserId(Authentication authentication) {
    try {
      return UUID.fromString(authentication.getName());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing gateway identity", ex);
    }
  }

  private @Nullable String normalizeReviewNote(@Nullable String reviewNote) {
    if (reviewNote == null) {
      return null;
    }
    String trimmed = reviewNote.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private @Nullable String normalizeNote(@Nullable String note) {
    if (note == null) {
      return null;
    }
    String trimmed = note.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private AdoptionDtos.AdoptionRequestView toView(AdoptionRequest request) {
    return new AdoptionDtos.AdoptionRequestView(
      request.getId(),
      request.getAnimalId(),
      request.getAdopterId(),
      request.getStatus(),
      request.getReviewedBy(),
      request.getReviewNote(),
      request.getNote(),
      request.getCreatedAt(),
      request.getUpdatedAt()
    );
  }
}
