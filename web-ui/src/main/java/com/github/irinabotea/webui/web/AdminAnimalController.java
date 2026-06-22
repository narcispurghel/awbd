package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.AnimalServiceClient;
import com.github.irinabotea.webui.client.BackendDtos;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos;
import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.web.form.AnimalForm;
import com.github.irinabotea.webui.web.form.MedicalRecordForm;
import jakarta.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/animals")
public class AdminAnimalController {

  private final AnimalServiceClient animals;

  public AdminAnimalController(AnimalServiceClient animals) {
    this.animals = animals;
  }

  @InitBinder({"animalForm", "medicalRecordForm"})
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
    populateForm(model, null);
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
      populateForm(model, null);
      return "admin/animals/form";
    }
    try {
      AnimalDtos.AnimalView created = animals.createAnimal(toRequest(form));
      flash.addFlashAttribute("success", "Animal created.");
      return "redirect:/animals/" + created.id();
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      populateForm(model, null);
      return "admin/animals/form";
    }
  }

  @GetMapping("/{id}/edit")
  public String editPage(@PathVariable UUID id, Model model) {
    if (!model.containsAttribute("animalForm")) {
      AnimalDtos.AnimalView existing;
      try {
        existing = animals.get(id);
      } catch (BackendException ex) {
        return "redirect:/animals";
      }
      model.addAttribute("animalForm", toForm(existing));
    }
    populateForm(model, id);
    try {
      model.addAttribute("photos", animals.photos(id));
    } catch (BackendException ignored) {
      model.addAttribute("photos", List.of());
    }
    return "admin/animals/form";
  }

  @GetMapping("/{animalId}/medical-records/{recordId}/edit")
  public String editMedicalRecordPage(
    @PathVariable UUID animalId,
    @PathVariable UUID recordId,
    Model model
  ) {
    AnimalDtos.AnimalView animal;
    try {
      animal = animals.get(animalId);
    } catch (BackendException ex) {
      return "redirect:/animals";
    }
    if (!model.containsAttribute("medicalRecordForm")) {
      try {
        model.addAttribute("medicalRecordForm", toForm(animals.getMedicalRecord(animalId, recordId)));
      } catch (BackendException ex) {
        return "redirect:/animals/" + animalId + "#medical";
      }
    }
    model.addAttribute("animal", animal);
    model.addAttribute("recordId", recordId);
    return "admin/animals/medical-record-form";
  }

  @PostMapping("/{id}")
  public String update(
    @PathVariable UUID id,
    @Valid @ModelAttribute("animalForm") AnimalForm form,
    BindingResult binding,
    Model model,
    RedirectAttributes flash
  ) {
    if (binding.hasErrors()) {
      populateForm(model, id);
      return "admin/animals/form";
    }
    try {
      animals.updateAnimal(id, toRequest(form));
      flash.addFlashAttribute("success", "Animal updated.");
      return "redirect:/animals/" + id;
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      populateForm(model, id);
      return "admin/animals/form";
    }
  }

  @PostMapping("/{id}/photos")
  public String uploadPhoto(
    @PathVariable UUID id,
    @RequestParam("file") MultipartFile file,
    RedirectAttributes flash
  ) {
    if (file.isEmpty()) {
      flash.addFlashAttribute("error", "Choose an image file before uploading.");
      return "redirect:/admin/animals/" + id + "/edit#photos";
    }
    try {
      byte[] bytes = file.getBytes();
      String contentType = file.getContentType();
      if (contentType == null || !contentType.startsWith("image/")) {
        flash.addFlashAttribute("error", "Only image files are allowed.");
        return "redirect:/admin/animals/" + id + "/edit#photos";
      }
      String filename = file.getOriginalFilename();
      animals.uploadPhoto(
        id,
        filename == null || filename.isBlank() ? "upload" : filename,
        contentType,
        bytes
      );
      flash.addFlashAttribute("success", "Photo uploaded.");
    } catch (IOException ex) {
      flash.addFlashAttribute("error", "Could not read the uploaded file.");
    } catch (BackendException ex) {
      flash.addFlashAttribute("error", ex.safeMessage());
    }
    return "redirect:/admin/animals/" + id + "/edit#photos";
  }

  @PostMapping("/{id}/photos/{photoId}/delete")
  public String deletePhoto(
    @PathVariable UUID id,
    @PathVariable UUID photoId,
    RedirectAttributes flash
  ) {
    try {
      animals.deletePhoto(id, photoId);
      flash.addFlashAttribute("success", "Photo removed.");
    } catch (BackendException ex) {
      flash.addFlashAttribute("error", ex.safeMessage());
    }
    return "redirect:/admin/animals/" + id + "/edit#photos";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable UUID id, RedirectAttributes flash) {
    try {
      animals.deleteAnimal(id);
      flash.addFlashAttribute("success", "Animal deleted.");
      return "redirect:/animals";
    } catch (BackendException ex) {
      flash.addFlashAttribute("error", ex.safeMessage());
      return "redirect:/animals/" + id;
    }
  }

  @PostMapping("/{animalId}/medical-records/{recordId}/delete")
  public String deleteMedicalRecord(
    @PathVariable UUID animalId,
    @PathVariable UUID recordId,
    RedirectAttributes flash
  ) {
    try {
      animals.deleteMedicalRecord(animalId, recordId);
      flash.addFlashAttribute("success", "Medical record deleted.");
    } catch (BackendException ex) {
      flash.addFlashAttribute("error", ex.safeMessage());
    }
    return "redirect:/animals/" + animalId + "#medical";
  }

  @PostMapping("/{animalId}/medical-records/{recordId}")
  public String updateMedicalRecord(
    @PathVariable UUID animalId,
    @PathVariable UUID recordId,
    @Valid @ModelAttribute("medicalRecordForm") MedicalRecordForm form,
    BindingResult binding,
    Model model,
    RedirectAttributes flash
  ) {
    if (binding.hasErrors()) {
      try {
        model.addAttribute("animal", animals.get(animalId));
      } catch (BackendException ex) {
        return "redirect:/animals";
      }
      model.addAttribute("recordId", recordId);
      return "admin/animals/medical-record-form";
    }
    try {
      animals.updateMedicalRecord(animalId, recordId, toMedicalRequest(form));
      flash.addFlashAttribute("success", "Medical record updated.");
      return "redirect:/animals/" + animalId + "#medical";
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      try {
        model.addAttribute("animal", animals.get(animalId));
      } catch (BackendException ignored) {
        return "redirect:/animals";
      }
      model.addAttribute("recordId", recordId);
      return "admin/animals/medical-record-form";
    }
  }

  @PostMapping("/{id}/medical-records")
  public String addMedicalRecord(
    @PathVariable UUID id,
    @Valid @ModelAttribute("medicalRecordForm") MedicalRecordForm form,
    BindingResult binding,
    RedirectAttributes flash
  ) {
    if (binding.hasErrors()) {
      flashFormErrors(flash, form, binding);
      return "redirect:/animals/" + id + "#medical";
    }
    try {
      animals.addMedicalRecord(id, toMedicalRequest(form));
      flash.addFlashAttribute("success", "Medical record added.");
      return "redirect:/animals/" + id + "#medical";
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      flashFormErrors(flash, form, binding);
      return "redirect:/animals/" + id + "#medical";
    }
  }

  private void populateForm(Model model, @org.jspecify.annotations.Nullable UUID id) {
    model.addAttribute("animalId", id);
    model.addAttribute("shelterOptions", animals.shelters());
    model.addAttribute("speciesOptions", animals.species());
    model.addAttribute("breedOptions", animals.breeds(null));
    model.addAttribute("tagOptions", animals.tags());
    model.addAttribute("statuses", AnimalDtos.AnimalStatus.values());
    model.addAttribute("sexes", AnimalDtos.Sex.values());
    model.addAttribute(
      "pageTitle",
      id == null ? "Admin \u00B7 New animal" : "Admin \u00B7 Edit animal"
    );
    model.addAttribute("heading", id == null ? "New animal" : "Edit animal");
    model.addAttribute("submitLabel", id == null ? "Create animal" : "Save changes");
    model.addAttribute("crumbLabel", id == null ? "New" : "Edit");
  }

  private static AnimalForm toForm(AnimalDtos.AnimalView v) {
    AnimalForm f = new AnimalForm();
    f.setName(v.name());
    f.setShelterId(v.shelterId());
    f.setSpeciesId(v.speciesId());
    f.setBreedId(v.breedId());
    f.setStatus(v.status());
    f.setSex(v.sex());
    f.setDescription(v.description());
    f.setBirthDate(v.birthDate());
    f.setIntakeDate(v.intakeDate());
    f.setAdoptionFee(v.adoptionFee());
    f.setVaccinated(v.vaccinated());
    f.setNeutered(v.neutered());
    f.setTagIds(v.tags().stream().map(AnimalDtos.TagView::id).toList());
    return f;
  }

  private static MedicalRecordForm toForm(AnimalDtos.MedicalRecordView v) {
    MedicalRecordForm f = new MedicalRecordForm();
    f.setTitle(v.title());
    f.setExaminationDate(v.examinationDate());
    f.setTreatment(v.treatment());
    f.setNotes(v.notes());
    f.setWeightKg(v.weightKg());
    f.setFollowUpRequired(v.followUpRequired());
    return f;
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
      f.isNeutered(),
      f.getTagIds()
    );
  }

  private static AnimalDtos.UpsertMedicalRecordRequest toMedicalRequest(MedicalRecordForm f) {
    return new AnimalDtos.UpsertMedicalRecordRequest(
      f.getTitle(),
      requireNonNull(f.getExaminationDate(), "examinationDate"),
      blankToNull(f.getTreatment()),
      blankToNull(f.getNotes()),
      f.getWeightKg(),
      f.isFollowUpRequired()
    );
  }

  private static void flashFormErrors(
    RedirectAttributes flash,
    MedicalRecordForm form,
    BindingResult binding
  ) {
    flash.addFlashAttribute("medicalRecordForm", form);
    flash.addFlashAttribute(
      "org.springframework.validation.BindingResult.medicalRecordForm",
      binding
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
