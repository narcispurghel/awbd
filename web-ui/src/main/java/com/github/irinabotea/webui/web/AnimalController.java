package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.AdoptionServiceClient;
import com.github.irinabotea.webui.client.AnimalServiceClient;
import com.github.irinabotea.webui.client.BackendDtos;
import com.github.irinabotea.webui.client.BackendDtos.AdoptionDtos;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos.AnimalStatus;
import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.client.BackendHttpClient;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/animals")
public class AnimalController {

  private final AnimalServiceClient animals;
  private final AdoptionServiceClient adoptions;

  public AnimalController(AnimalServiceClient animals, AdoptionServiceClient adoptions) {
    this.animals = animals;
    this.adoptions = adoptions;
  }

  @GetMapping
  public String list(
    @RequestParam(name = "status", required = false) @Nullable String statusRaw,
    @RequestParam(name = "speciesId", required = false) @Nullable String speciesIdRaw,
    @RequestParam(name = "shelterId", required = false) @Nullable String shelterIdRaw,
    @RequestParam(name = "page", defaultValue = "0") int page,
    @RequestParam(name = "size", defaultValue = "10") int size,
    @RequestParam(name = "sort", defaultValue = "name,asc") String sort,
    Model model
  ) {
    AnimalStatus status = parseEnum(statusRaw, AnimalStatus.class);
    UUID speciesId = parseUuid(speciesIdRaw);
    UUID shelterId = parseUuid(shelterIdRaw);

    BackendDtos.PageResponse<AnimalDtos.AnimalSummary> animalsPage =
      animals.page(status, speciesId, shelterId, page, size, sort);

    model.addAttribute("animalsPage", animalsPage);
    model.addAttribute("animals", animalsPage.content());
    model.addAttribute("species", animals.species());
    model.addAttribute("shelters", animals.shelters());
    model.addAttribute("statuses", AnimalDtos.AnimalStatus.values());
    model.addAttribute("selectedStatus", status);
    model.addAttribute("selectedSpeciesId", speciesId);
    model.addAttribute("selectedShelterId", shelterId);
    model.addAttribute("selectedSize", size);
    model.addAttribute("selectedSort", sort);
    model.addAttribute("pageBaseUrl", "/animals");
    model.addAttribute("pageQuery", animalsQuery(status, speciesId, shelterId, size, sort));
    return "animals/list";
  }

  private static String animalsQuery(
    AnimalDtos.@Nullable AnimalStatus status,
    @Nullable UUID speciesId,
    @Nullable UUID shelterId,
    int size,
    String sort
  ) {
    StringBuilder q = new StringBuilder();
    if (status != null) q.append("status=").append(status.name()).append('&');
    if (speciesId != null) q.append("speciesId=").append(speciesId).append('&');
    if (shelterId != null) q.append("shelterId=").append(shelterId).append('&');
    q.append("size=").append(size).append("&sort=").append(sort);
    return q.toString();
  }

  @GetMapping("/{id}")
  public String view(
    @PathVariable UUID id,
    @RequestParam(name = "mrPage", defaultValue = "0") int mrPage,
    @RequestParam(name = "mrSize", defaultValue = "5") int mrSize,
    Model model,
    @Nullable Authentication auth
  ) {
    AnimalDtos.AnimalView animal = animals.get(id);
    model.addAttribute("animal", animal);
    BackendDtos.PageResponse<AnimalDtos.MedicalRecordView> medicalPage =
      animals.medicalRecordsPage(id, mrPage, mrSize);
    model.addAttribute("medicalRecordsPage", medicalPage);
    model.addAttribute("medicalRecords", medicalPage.content());
    model.addAttribute("mrPageBaseUrl", "/animals/" + id);
    model.addAttribute("mrPageQuery", "mrSize=" + mrSize);
    model.addAttribute("photos", animals.photos(id));
    if (!model.containsAttribute("medicalRecordForm")) {
      model.addAttribute(
        "medicalRecordForm",
        new com.github.irinabotea.webui.web.form.MedicalRecordForm()
      );
    }
    model.addAttribute("existingPendingRequest", findOwnPending(animal, auth));
    return "animals/view";
  }

  @GetMapping("/{animalId}/photos/{photoId}")
  public ResponseEntity<byte[]> photoBytes(
    @PathVariable UUID animalId,
    @PathVariable UUID photoId
  ) {
    try {
      BackendHttpClient.BytesResponse p = animals.photoBytes(animalId, photoId);
      return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(p.contentType()))
        .body(p.bytes());
    } catch (BackendException ex) {
      return ResponseEntity.status(ex.status()).build();
    }
  }

  private AdoptionDtos.@Nullable AdoptionRequestView findOwnPending(
    AnimalDtos.AnimalView animal,
    @Nullable Authentication auth
  ) {
    if (auth == null || animal.status() != AnimalStatus.AVAILABLE || isAdmin(auth)) {
      return null;
    }
    try {
      for (AdoptionDtos.AdoptionRequestView r : adoptions.mine()) {
        if (
          r.status() == AdoptionDtos.AdoptionRequestStatus.PENDING
            && animal.id().equals(r.animalId())
        ) {
          return r;
        }
      }
    } catch (BackendException ignored) {
      // Best-effort: if adoption-service is unreachable, fall back to showing the button.
    }
    return null;
  }

  private static boolean isAdmin(Authentication auth) {
    return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
  }

  private static <E extends Enum<E>> @Nullable E parseEnum(@Nullable String value, Class<E> type) {
    if (value == null || value.isBlank()) return null;
    try {
      return Enum.valueOf(type, value);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  private static @Nullable UUID parseUuid(@Nullable String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
