package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.AdoptionServiceClient;
import com.github.irinabotea.webui.client.AnimalServiceClient;
import com.github.irinabotea.webui.client.BackendDtos.AdoptionDtos;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos;
import com.github.irinabotea.webui.client.BackendException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/adoptions")
public class AdoptionController {

  private final AdoptionServiceClient adoptions;
  private final AnimalServiceClient animals;

  public AdoptionController(AdoptionServiceClient adoptions, AnimalServiceClient animals) {
    this.adoptions = adoptions;
    this.animals = animals;
  }

  @PostMapping("/request/{animalId}")
  public String request(
    @PathVariable UUID animalId,
    @RequestParam(name = "note", required = false) @Nullable String note,
    RedirectAttributes flash
  ) {
    try {
      adoptions.requestAdoption(animalId, blankToNull(note));
      flash.addFlashAttribute("success", "Adoption request submitted.");
      return "redirect:/adoptions/me";
    } catch (BackendException ex) {
      flash.addFlashAttribute("error", ex.safeMessage());
      return "redirect:/animals/" + animalId;
    }
  }

  private static @Nullable String blankToNull(@Nullable String s) {
    return (s == null || s.isBlank()) ? null : s.trim();
  }

  @PostMapping("/{id}/cancel")
  public String cancel(@PathVariable UUID id, RedirectAttributes flash) {
    try {
      adoptions.cancel(id);
      flash.addFlashAttribute("success", "Adoption request cancelled.");
    } catch (BackendException ex) {
      flash.addFlashAttribute("error", ex.safeMessage());
    }
    return "redirect:/adoptions/me";
  }

  @GetMapping("/me")
  public String mine(Model model, @Nullable Authentication auth) {
    if (auth != null && isAdmin(auth)) {
      return "redirect:/admin/adoptions";
    }
    List<AdoptionDtos.AdoptionRequestView> requests = adoptions.mine();
    List<MyAdoptionRow> rows = new ArrayList<>(requests.size());
    for (AdoptionDtos.AdoptionRequestView r : requests) {
      rows.add(new MyAdoptionRow(r, fetchAnimal(r.animalId())));
    }
    model.addAttribute("rows", rows);
    return "adoptions/me";
  }

  private static boolean isAdmin(Authentication auth) {
    return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
  }

  private AnimalDtos.@Nullable AnimalView fetchAnimal(UUID id) {
    try {
      return animals.get(id);
    } catch (BackendException ex) {
      return null;
    }
  }

  public record MyAdoptionRow(
    AdoptionDtos.AdoptionRequestView request,
    AnimalDtos.@Nullable AnimalView animal
  ) {}
}
