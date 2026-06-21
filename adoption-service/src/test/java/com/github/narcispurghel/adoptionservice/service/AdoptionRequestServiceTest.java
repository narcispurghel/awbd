package com.github.narcispurghel.adoptionservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.narcispurghel.adoptionservice.dto.AdoptionDtos;
import com.github.narcispurghel.adoptionservice.entity.AdoptionRequest;
import com.github.narcispurghel.adoptionservice.entity.AdoptionRequestStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import com.github.narcispurghel.adoptionservice.repository.AdoptionRequestJpaRepository;
import org.springframework.web.server.ResponseStatusException;

class AdoptionRequestServiceTest {

  private final AdoptionRequestJpaRepository repository =
    mock(AdoptionRequestJpaRepository.class);
  private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

  private final AdoptionRequestService service =
    new AdoptionRequestService(repository, publisher);

  private final UUID adopterId = UUID.randomUUID();
  private final UUID adminId = UUID.randomUUID();

  @Test
  void create_savesPendingAndPublishesEvent() {
    UUID animalId = UUID.randomUUID();
    when(repository.save(any(AdoptionRequest.class))).thenAnswer(inv -> inv.getArgument(0));

    AdoptionDtos.AdoptionRequestView view = service.create(
      userAuth(),
      new AdoptionDtos.CreateAdoptionRequest(animalId, "  please  ")
    );

    assertThat(view.status()).isEqualTo(AdoptionRequestStatus.PENDING);
    assertThat(view.animalId()).isEqualTo(animalId);
    assertThat(view.adopterId()).isEqualTo(adopterId);
    assertThat(view.note()).isEqualTo("please");
    verify(publisher).publishEvent(any(Object.class));
  }

  @Test
  void get_asNonOwner_throws403() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id))
      .thenReturn(Optional.of(request(id, UUID.randomUUID(), AdoptionRequestStatus.PENDING)));

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.get(userAuth(), id),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(403);
  }

  @Test
  void get_asAdmin_returnsAnyRequest() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id))
      .thenReturn(Optional.of(request(id, UUID.randomUUID(), AdoptionRequestStatus.PENDING)));

    AdoptionDtos.AdoptionRequestView view = service.get(adminAuth(), id);

    assertThat(view.id()).isEqualTo(id);
  }

  @Test
  void review_approve_publishesApprovedAndSetsStatus() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id))
      .thenReturn(Optional.of(request(id, adopterId, AdoptionRequestStatus.PENDING)));
    when(repository.save(any(AdoptionRequest.class))).thenAnswer(inv -> inv.getArgument(0));

    AdoptionDtos.AdoptionRequestView view = service.review(
      adminAuth(),
      id,
      new AdoptionDtos.ReviewAdoptionRequest(AdoptionRequestStatus.APPROVED, "Welcome")
    );

    assertThat(view.status()).isEqualTo(AdoptionRequestStatus.APPROVED);
    verify(publisher).publishEvent(any(Object.class));
  }

  @Test
  void review_invalidStatus_throws400() {
    ResponseStatusException ex = catchThrowableOfType(
      () ->
        service.review(
          adminAuth(),
          UUID.randomUUID(),
          new AdoptionDtos.ReviewAdoptionRequest(AdoptionRequestStatus.PENDING, null)
        ),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(400);
  }

  @Test
  void review_asNonAdmin_throws403() {
    ResponseStatusException ex = catchThrowableOfType(
      () ->
        service.review(
          userAuth(),
          UUID.randomUUID(),
          new AdoptionDtos.ReviewAdoptionRequest(AdoptionRequestStatus.APPROVED, null)
        ),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(403);
  }

  @Test
  void cancel_pendingOwnRequest_setsCancelled() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id))
      .thenReturn(Optional.of(request(id, adopterId, AdoptionRequestStatus.PENDING)));
    when(repository.save(any(AdoptionRequest.class))).thenAnswer(inv -> inv.getArgument(0));

    AdoptionDtos.AdoptionRequestView view = service.cancel(userAuth(), id);

    assertThat(view.status()).isEqualTo(AdoptionRequestStatus.CANCELLED);
    verify(publisher).publishEvent(any(Object.class));
  }

  @Test
  void cancel_nonPending_throws409() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id))
      .thenReturn(Optional.of(request(id, adopterId, AdoptionRequestStatus.APPROVED)));

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.cancel(userAuth(), id),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(409);
  }

  @Test
  void delete_asAdmin_removesRequest() {
    UUID id = UUID.randomUUID();
    AdoptionRequest request = request(id, adopterId, AdoptionRequestStatus.REJECTED);
    when(repository.findById(id)).thenReturn(Optional.of(request));

    service.delete(adminAuth(), id);

    verify(repository).delete(request);
  }

  @Test
  void delete_asNonAdmin_throws403() {
    ResponseStatusException ex = catchThrowableOfType(
      () -> service.delete(userAuth(), UUID.randomUUID()),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(403);
    verify(repository, never()).delete(any(AdoptionRequest.class));
  }

  @Test
  void list_asAdmin_returnsMappedViews() {
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
      .thenReturn(
        new PageImpl<>(List.of(request(UUID.randomUUID(), adopterId, AdoptionRequestStatus.PENDING)))
      );

    Page<AdoptionDtos.AdoptionRequestView> views =
      service.list(adminAuth(), null, null, Pageable.unpaged());

    assertThat(views.getContent()).hasSize(1);
  }

  // ----- helpers -----

  private Authentication userAuth() {
    return new UsernamePasswordAuthenticationToken(
      adopterId.toString(),
      null,
      List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );
  }

  private Authentication adminAuth() {
    return new UsernamePasswordAuthenticationToken(
      adminId.toString(),
      null,
      List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
    );
  }

  private static AdoptionRequest request(UUID id, UUID adopterId, AdoptionRequestStatus status) {
    AdoptionRequest request = new AdoptionRequest();
    request.setAnimalId(UUID.randomUUID());
    request.setAdopterId(adopterId);
    request.setStatus(status);
    ReflectionTestUtils.setField(request, "id", id);
    return request;
  }
}
