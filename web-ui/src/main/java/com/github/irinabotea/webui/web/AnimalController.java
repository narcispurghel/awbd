package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.AnimalServiceClient;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos.AnimalStatus;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
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

  public AnimalController(AnimalServiceClient animals) {
    this.animals = animals;
  }

  @GetMapping
  public String list(
    @RequestParam(name = "status", required = false) @Nullable String statusRaw,
    @RequestParam(name = "speciesId", required = false) @Nullable String speciesIdRaw,
    @RequestParam(name = "shelterId", required = false) @Nullable String shelterIdRaw,
    Model model
  ) {
    AnimalStatus status = parseEnum(statusRaw, AnimalStatus.class);
    UUID speciesId = parseUuid(speciesIdRaw);
    UUID shelterId = parseUuid(shelterIdRaw);

    model.addAttribute("animals", animals.list(status, speciesId, shelterId));
    model.addAttribute("species", animals.species());
    model.addAttribute("shelters", animals.shelters());
    model.addAttribute("statuses", AnimalDtos.AnimalStatus.values());
    model.addAttribute("selectedStatus", status);
    model.addAttribute("selectedSpeciesId", speciesId);
    model.addAttribute("selectedShelterId", shelterId);
    return "animals/list";
  }

  @GetMapping("/{id}")
  public String view(@PathVariable UUID id, Model model) {
    model.addAttribute("animal", animals.get(id));
    model.addAttribute("medicalRecords", animals.medicalRecords(id));
    if (!model.containsAttribute("medicalRecordForm")) {
      model.addAttribute(
        "medicalRecordForm",
        new com.github.irinabotea.webui.web.form.MedicalRecordForm()
      );
    }
    return "animals/view";
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
