package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.AdoptionServiceClient;
import com.github.irinabotea.webui.client.AnimalServiceClient;
import com.github.irinabotea.webui.client.BackendDtos.AdoptionDtos;
import com.github.irinabotea.webui.client.BackendDtos.AdoptionDtos.AdoptionRequestStatus;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/adoptions")
public class AdminAdoptionController {

  private final AdoptionServiceClient adoptions;
  private final AnimalServiceClient animals;

  public AdminAdoptionController(AdoptionServiceClient adoptions, AnimalServiceClient animals) {
    this.adoptions = adoptions;
    this.animals = animals;
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

    model.addAttribute("requests", requests);
    model.addAttribute("animalIndex", animalIndex);
    model.addAttribute("animalOptions", allAnimals);
    model.addAttribute("statuses", AdoptionRequestStatus.values());
    model.addAttribute("selectedStatus", status);
    model.addAttribute("selectedAnimalId", animalId);
    return "admin/adoptions/list";
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
