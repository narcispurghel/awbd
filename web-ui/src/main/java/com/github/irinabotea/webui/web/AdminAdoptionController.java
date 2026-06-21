package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.AdoptionServiceClient;
import com.github.irinabotea.webui.client.AnimalServiceClient;
import com.github.irinabotea.webui.client.BackendDtos;
import com.github.irinabotea.webui.client.BackendDtos.AdoptionDtos;
import com.github.irinabotea.webui.client.BackendDtos.AdoptionDtos.AdoptionRequestStatus;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos;
import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.client.UserServiceClient;
import com.github.irinabotea.webui.web.form.ReviewForm;
import jakarta.validation.Valid;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/adoptions")
public class AdminAdoptionController {

  private static final EnumSet<AdoptionRequestStatus> REVIEW_DECISIONS =
    EnumSet.of(AdoptionRequestStatus.APPROVED, AdoptionRequestStatus.REJECTED);

  private final AdoptionServiceClient adoptions;
  private final AnimalServiceClient animals;
  private final UserServiceClient users;

  public AdminAdoptionController(
    AdoptionServiceClient adoptions,
    AnimalServiceClient animals,
    UserServiceClient users
  ) {
    this.adoptions = adoptions;
    this.animals = animals;
    this.users = users;
  }

  @GetMapping
  public String list(
    @RequestParam(name = "status", required = false) @Nullable String statusRaw,
    @RequestParam(name = "animalId", required = false) @Nullable String animalIdRaw,
    Model model
  ) {
    AdoptionRequestStatus status = parseEnum(statusRaw, AdoptionRequestStatus.class);
    UUID animalId = parseUuid(animalIdRaw);

    List<AdoptionDtos.AdoptionRequestView> requests = adoptions.list(status, animalId);
    List<AnimalDtos.AnimalSummary> allAnimals = animals.list(null, null, null);
    Map<UUID, AnimalDtos.AnimalSummary> animalIndex = new HashMap<>(allAnimals.size());
    for (AnimalDtos.AnimalSummary a : allAnimals) {
      animalIndex.put(a.id(), a);
    }

    Set<UUID> adopterIds = new HashSet<>();
    for (AdoptionDtos.AdoptionRequestView r : requests) {
      adopterIds.add(r.adopterId());
    }
    Map<UUID, BackendDtos.CurrentUser> adopterIndex = new HashMap<>(adopterIds.size());
    for (UUID adopterId : adopterIds) {
      try {
        adopterIndex.put(adopterId, users.getUserById(adopterId));
      } catch (BackendException ignored) {
        // user lookup failures fall back to raw UUID display
      }
    }

    model.addAttribute("requests", requests);
    model.addAttribute("animalIndex", animalIndex);
    model.addAttribute("adopterIndex", adopterIndex);
    model.addAttribute("animalOptions", allAnimals);
    model.addAttribute("statuses", AdoptionRequestStatus.values());
    model.addAttribute("selectedStatus", status);
    model.addAttribute("selectedAnimalId", animalId);
    return "admin/adoptions/list";
  }

  @GetMapping("/{id}")
  public String view(@PathVariable UUID id, Model model) {
    AdoptionDtos.AdoptionRequestView request;
    try {
      request = adoptions.get(id);
    } catch (BackendException ex) {
      return "redirect:/admin/adoptions";
    }
    populateView(model, request);
    if (!model.containsAttribute("reviewForm")) {
      model.addAttribute("reviewForm", new ReviewForm());
    }
    return "admin/adoptions/view";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable UUID id, RedirectAttributes flash) {
    try {
      adoptions.delete(id);
      flash.addFlashAttribute("success", "Adoption request deleted.");
    } catch (BackendException ex) {
      flash.addFlashAttribute("error", ex.safeMessage());
    }
    return "redirect:/admin/adoptions";
  }

  @PostMapping("/{id}/review")
  public String review(
    @PathVariable UUID id,
    @Valid @ModelAttribute("reviewForm") ReviewForm form,
    BindingResult binding,
    Model model,
    RedirectAttributes flash
  ) {
    if (form.getStatus() != null && !REVIEW_DECISIONS.contains(form.getStatus())) {
      binding.rejectValue("status", "invalid", "Review must be Approve or Reject");
    }
    if (binding.hasErrors()) {
      populateViewSafely(model, id);
      return "admin/adoptions/view";
    }
    try {
      AdoptionRequestStatus decision = form.getStatus();
      if (decision == null) {
        throw new IllegalStateException("status must be validated before mapping");
      }
      adoptions.review(
        id,
        new AdoptionDtos.ReviewAdoptionRequest(decision, blankToNull(form.getReviewNote()))
      );
      flash.addFlashAttribute(
        "success",
        decision == AdoptionRequestStatus.APPROVED
          ? "Adoption request approved."
          : "Adoption request rejected."
      );
      return "redirect:/admin/adoptions";
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      populateViewSafely(model, id);
      return "admin/adoptions/view";
    }
  }

  private void populateViewSafely(Model model, UUID id) {
    try {
      populateView(model, adoptions.get(id));
    } catch (BackendException ignored) {
      // Best-effort; the page will at least show validation errors via the form.
    }
  }

  private void populateView(Model model, AdoptionDtos.AdoptionRequestView request) {
    model.addAttribute("request", request);
    model.addAttribute("animal", fetchAnimal(request.animalId()));
    model.addAttribute("adopter", fetchUser(request.adopterId()));
  }

  private AnimalDtos.@Nullable AnimalView fetchAnimal(UUID id) {
    try {
      return animals.get(id);
    } catch (BackendException ex) {
      return null;
    }
  }

  private BackendDtos.@Nullable CurrentUser fetchUser(UUID id) {
    try {
      return users.getUserById(id);
    } catch (BackendException ex) {
      return null;
    }
  }

  private static void applyBackendErrors(BindingResult binding, BackendException ex) {
    for (BackendDtos.FieldError fe : ex.fieldErrors()) {
      binding.rejectValue(fe.field(), "backend", fe.message());
    }
    if (ex.fieldErrors().isEmpty()) {
      binding.reject("backend", ex.safeMessage());
    }
  }

  private static @Nullable String blankToNull(@Nullable String s) {
    return (s == null || s.isBlank()) ? null : s;
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
