package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.AnimalServiceClient;
import com.github.irinabotea.webui.client.BackendDtos;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos;
import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.web.form.BreedForm;
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
@RequestMapping("/admin/breeds")
public class AdminBreedController {

  private final AnimalServiceClient animals;

  public AdminBreedController(AnimalServiceClient animals) {
    this.animals = animals;
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("breeds", animals.breeds(null));
    return "admin/breeds/list";
  }

  @GetMapping("/new")
  public String createPage(Model model) {
    if (!model.containsAttribute("breedForm")) {
      model.addAttribute("breedForm", new BreedForm());
    }
    populateForm(model, null);
    return "admin/breeds/form";
  }

  @PostMapping
  public String create(
    @Valid @ModelAttribute("breedForm") BreedForm form,
    BindingResult binding,
    Model model,
    RedirectAttributes flash
  ) {
    if (binding.hasErrors()) {
      populateForm(model, null);
      return "admin/breeds/form";
    }
    try {
      animals.createBreed(toRequest(form));
      flash.addFlashAttribute("success", "Breed created.");
      return "redirect:/admin/breeds";
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      populateForm(model, null);
      return "admin/breeds/form";
    }
  }

  @GetMapping("/{id}/edit")
  public String editPage(@PathVariable UUID id, Model model) {
    if (!model.containsAttribute("breedForm")) {
      AnimalDtos.@org.jspecify.annotations.Nullable BreedView existing = animals.findBreed(id);
      if (existing == null) {
        return "redirect:/admin/breeds";
      }
      BreedForm f = new BreedForm();
      f.setSpeciesId(existing.speciesId());
      f.setName(existing.name());
      model.addAttribute("breedForm", f);
    }
    populateForm(model, id);
    return "admin/breeds/form";
  }

  @PostMapping("/{id}")
  public String update(
    @PathVariable UUID id,
    @Valid @ModelAttribute("breedForm") BreedForm form,
    BindingResult binding,
    Model model,
    RedirectAttributes flash
  ) {
    if (binding.hasErrors()) {
      populateForm(model, id);
      return "admin/breeds/form";
    }
    try {
      animals.updateBreed(id, toRequest(form));
      flash.addFlashAttribute("success", "Breed updated.");
      return "redirect:/admin/breeds";
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      populateForm(model, id);
      return "admin/breeds/form";
    }
  }

  private void populateForm(Model model, @org.jspecify.annotations.Nullable UUID id) {
    model.addAttribute("breedId", id);
    model.addAttribute("speciesOptions", animals.species());
    model.addAttribute(
      "pageTitle",
      id == null ? "Admin \u00B7 New breed" : "Admin \u00B7 Edit breed"
    );
    model.addAttribute("heading", id == null ? "New breed" : "Edit breed");
    model.addAttribute("submitLabel", id == null ? "Create breed" : "Save changes");
    model.addAttribute("crumbLabel", id == null ? "New" : "Edit");
  }

  private static AnimalDtos.UpsertBreedRequest toRequest(BreedForm form) {
    UUID speciesId = form.getSpeciesId();
    if (speciesId == null) {
      throw new IllegalStateException("speciesId must be validated before mapping");
    }
    return new AnimalDtos.UpsertBreedRequest(speciesId, form.getName());
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
