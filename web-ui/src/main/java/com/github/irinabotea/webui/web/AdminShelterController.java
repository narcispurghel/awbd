package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.AnimalServiceClient;
import com.github.irinabotea.webui.client.BackendDtos;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos;
import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.web.form.ShelterForm;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/shelters")
public class AdminShelterController {

  private final AnimalServiceClient animals;

  public AdminShelterController(AnimalServiceClient animals) {
    this.animals = animals;
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("shelters", animals.shelters());
    return "admin/shelters/list";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable UUID id, RedirectAttributes flash) {
    try {
      animals.deleteShelter(id);
      flash.addFlashAttribute("success", "Shelter deleted.");
    } catch (BackendException ex) {
      flash.addFlashAttribute("error", ex.safeMessage());
    }
    return "redirect:/admin/shelters";
  }

  @GetMapping("/new")
  public String createPage(Model model) {
    if (!model.containsAttribute("shelterForm")) {
      model.addAttribute("shelterForm", new ShelterForm());
    }
    populateForm(model, null);
    return "admin/shelters/form";
  }

  @PostMapping
  public String create(
    @Valid @ModelAttribute("shelterForm") ShelterForm form,
    BindingResult binding,
    Model model,
    RedirectAttributes flash
  ) {
    if (binding.hasErrors()) {
      populateForm(model, null);
      return "admin/shelters/form";
    }
    try {
      animals.createShelter(toRequest(form));
      flash.addFlashAttribute("success", "Shelter created.");
      return "redirect:/admin/shelters";
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      populateForm(model, null);
      return "admin/shelters/form";
    }
  }

  @GetMapping("/{id}/edit")
  public String editPage(@PathVariable UUID id, Model model) {
    if (!model.containsAttribute("shelterForm")) {
      AnimalDtos.@org.jspecify.annotations.Nullable ShelterView existing = animals.findShelter(id);
      if (existing == null) {
        return "redirect:/admin/shelters";
      }
      model.addAttribute("shelterForm", toForm(existing));
    }
    populateForm(model, id);
    return "admin/shelters/form";
  }

  @PostMapping("/{id}")
  public String update(
    @PathVariable UUID id,
    @Valid @ModelAttribute("shelterForm") ShelterForm form,
    BindingResult binding,
    Model model,
    RedirectAttributes flash
  ) {
    if (binding.hasErrors()) {
      populateForm(model, id);
      return "admin/shelters/form";
    }
    try {
      animals.updateShelter(id, toRequest(form));
      flash.addFlashAttribute("success", "Shelter updated.");
      return "redirect:/admin/shelters";
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      populateForm(model, id);
      return "admin/shelters/form";
    }
  }

  private static void populateForm(Model model, @org.jspecify.annotations.Nullable UUID id) {
    model.addAttribute("shelterId", id);
    model.addAttribute(
      "pageTitle",
      id == null ? "Admin \u00B7 New shelter" : "Admin \u00B7 Edit shelter"
    );
    model.addAttribute("heading", id == null ? "New shelter" : "Edit shelter");
    model.addAttribute("submitLabel", id == null ? "Create shelter" : "Save changes");
    model.addAttribute("crumbLabel", id == null ? "New" : "Edit");
  }

  private static AnimalDtos.UpsertShelterRequest toRequest(ShelterForm form) {
    return new AnimalDtos.UpsertShelterRequest(
      form.getName(),
      form.getCity(),
      form.getCountryCode(),
      form.getContactEmail(),
      blankToNull(form.getContactPhone()),
      blankToNull(form.getAddress())
    );
  }

  private static ShelterForm toForm(AnimalDtos.ShelterView v) {
    ShelterForm f = new ShelterForm();
    f.setName(v.name());
    f.setCity(v.city());
    f.setCountryCode(v.countryCode());
    f.setContactEmail(v.contactEmail());
    f.setContactPhone(v.contactPhone());
    f.setAddress(v.address());
    return f;
  }

  private static void applyBackendErrors(BindingResult binding, BackendException ex) {
    for (BackendDtos.FieldError fe : ex.fieldErrors()) {
      binding.rejectValue(fe.field(), "backend", fe.message());
    }
    if (ex.fieldErrors().isEmpty()) {
      binding.reject("backend", ex.safeMessage());
    }
  }

  private static @org.jspecify.annotations.Nullable String blankToNull(
    @org.jspecify.annotations.Nullable String s
  ) {
    return (s == null || s.isBlank()) ? null : s;
  }
}
