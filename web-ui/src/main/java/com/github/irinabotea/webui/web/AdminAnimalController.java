package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.AnimalServiceClient;
import com.github.irinabotea.webui.client.BackendDtos;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos;
import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.web.form.AnimalForm;
import jakarta.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/animals")
public class AdminAnimalController {

  private final AnimalServiceClient animals;

  public AdminAnimalController(AnimalServiceClient animals) {
    this.animals = animals;
  }

  @InitBinder("animalForm")
  void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(UUID.class, new PropertyEditorSupport() {
      @Override
      public void setAsText(String text) {
        setValue((text == null || text.isBlank()) ? null : UUID.fromString(text));
      }
    });
    binder.registerCustomEditor(BigDecimal.class, new PropertyEditorSupport() {
      @Override
      public void setAsText(String text) {
        setValue((text == null || text.isBlank()) ? null : new BigDecimal(text));
      }
    });
  }

  @GetMapping("/new")
  public String createPage(Model model) {
    if (!model.containsAttribute("animalForm")) {
      model.addAttribute("animalForm", new AnimalForm());
    }
    populateForm(model);
    return "admin/animals/form";
  }

  @PostMapping
  public String create(
    @Valid @ModelAttribute("animalForm") AnimalForm form,
    BindingResult binding,
    Model model,
    RedirectAttributes flash
  ) {
    if (binding.hasErrors()) {
      populateForm(model);
      return "admin/animals/form";
    }
    try {
      AnimalDtos.AnimalView created = animals.createAnimal(toRequest(form));
      flash.addFlashAttribute("success", "Animal created.");
      return "redirect:/animals/" + created.id();
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      populateForm(model);
      return "admin/animals/form";
    }
  }

  private void populateForm(Model model) {
    model.addAttribute("shelterOptions", animals.shelters());
    model.addAttribute("speciesOptions", animals.species());
    model.addAttribute("breedOptions", animals.breeds(null));
    model.addAttribute("statuses", AnimalDtos.AnimalStatus.values());
    model.addAttribute("sexes", AnimalDtos.Sex.values());
    model.addAttribute("pageTitle", "Admin \u00B7 New animal");
    model.addAttribute("heading", "New animal");
    model.addAttribute("submitLabel", "Create animal");
  }

  private static AnimalDtos.UpsertAnimalRequest toRequest(AnimalForm f) {
    UUID shelterId = requireNonNull(f.getShelterId(), "shelterId");
    UUID speciesId = requireNonNull(f.getSpeciesId(), "speciesId");
    return new AnimalDtos.UpsertAnimalRequest(
      f.getName(),
      shelterId,
      speciesId,
      f.getBreedId(),
      f.getStatus(),
      f.getSex(),
      blankToNull(f.getDescription()),
      f.getBirthDate(),
      requireNonNull(f.getIntakeDate(), "intakeDate"),
      f.getAdoptionFee(),
      f.isVaccinated(),
      f.isNeutered()
    );
  }

  private static <T> T requireNonNull(@org.jspecify.annotations.Nullable T value, String name) {
    if (value == null) {
      throw new IllegalStateException(name + " must be validated before mapping");
    }
    return value;
  }

  private static @org.jspecify.annotations.Nullable String blankToNull(
    @org.jspecify.annotations.Nullable String s
  ) {
    return (s == null || s.isBlank()) ? null : s;
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
