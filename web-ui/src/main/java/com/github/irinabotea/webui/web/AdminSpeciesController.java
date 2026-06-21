package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.AnimalServiceClient;
import com.github.irinabotea.webui.client.BackendDtos;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos;
import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.web.form.SpeciesForm;
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
@RequestMapping("/admin/species")
public class AdminSpeciesController {

  private final AnimalServiceClient animals;

  public AdminSpeciesController(AnimalServiceClient animals) {
    this.animals = animals;
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("species", animals.species());
    return "admin/species/list";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable UUID id, RedirectAttributes flash) {
    try {
      animals.deleteSpecies(id);
      flash.addFlashAttribute("success", "Species deleted.");
    } catch (BackendException ex) {
      flash.addFlashAttribute("error", ex.safeMessage());
    }
    return "redirect:/admin/species";
  }

  @GetMapping("/new")
  public String createPage(Model model) {
    if (!model.containsAttribute("speciesForm")) {
      model.addAttribute("speciesForm", new SpeciesForm());
    }
    populateForm(model, null);
    return "admin/species/form";
  }

  @PostMapping
  public String create(
    @Valid @ModelAttribute("speciesForm") SpeciesForm form,
    BindingResult binding,
    Model model,
    RedirectAttributes flash
  ) {
    if (binding.hasErrors()) {
      populateForm(model, null);
      return "admin/species/form";
    }
    try {
      animals.createSpecies(new AnimalDtos.UpsertSpeciesRequest(form.getName()));
      flash.addFlashAttribute("success", "Species created.");
      return "redirect:/admin/species";
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      populateForm(model, null);
      return "admin/species/form";
    }
  }

  @GetMapping("/{id}/edit")
  public String editPage(@PathVariable UUID id, Model model) {
    if (!model.containsAttribute("speciesForm")) {
      AnimalDtos.@org.jspecify.annotations.Nullable SpeciesView existing = animals.findSpecies(id);
      if (existing == null) {
        return "redirect:/admin/species";
      }
      SpeciesForm f = new SpeciesForm();
      f.setName(existing.name());
      model.addAttribute("speciesForm", f);
    }
    populateForm(model, id);
    return "admin/species/form";
  }

  @PostMapping("/{id}")
  public String update(
    @PathVariable UUID id,
    @Valid @ModelAttribute("speciesForm") SpeciesForm form,
    BindingResult binding,
    Model model,
    RedirectAttributes flash
  ) {
    if (binding.hasErrors()) {
      populateForm(model, id);
      return "admin/species/form";
    }
    try {
      animals.updateSpecies(id, new AnimalDtos.UpsertSpeciesRequest(form.getName()));
      flash.addFlashAttribute("success", "Species updated.");
      return "redirect:/admin/species";
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      populateForm(model, id);
      return "admin/species/form";
    }
  }

  private static void populateForm(Model model, @org.jspecify.annotations.Nullable UUID id) {
    model.addAttribute("speciesId", id);
    model.addAttribute(
      "pageTitle",
      id == null ? "Admin \u00B7 New species" : "Admin \u00B7 Edit species"
    );
    model.addAttribute("heading", id == null ? "New species" : "Edit species");
    model.addAttribute("submitLabel", id == null ? "Create species" : "Save changes");
    model.addAttribute("crumbLabel", id == null ? "New" : "Edit");
  }

  private static void applyBackendErrors(BindingResult binding, BackendException ex) {
    for (BackendDtos.FieldError fe : ex.fieldErrors()) {
      binding.rejectValue(fe.field(), "backend", fe.message());
    }
    if (ex.fieldErrors().isEmpty()) {
      binding.reject("backend", ex.safeMessage());
    }
  }
}
