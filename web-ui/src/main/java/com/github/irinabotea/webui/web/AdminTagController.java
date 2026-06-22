package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.AnimalServiceClient;
import com.github.irinabotea.webui.client.BackendDtos;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos;
import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.web.form.TagForm;
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
@RequestMapping("/admin/tags")
public class AdminTagController {

  private final AnimalServiceClient animals;

  public AdminTagController(AnimalServiceClient animals) {
    this.animals = animals;
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("tags", animals.tags());
    return "admin/tags/list";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable UUID id, RedirectAttributes flash) {
    try {
      animals.deleteTag(id);
      flash.addFlashAttribute("success", "Tag deleted.");
    } catch (BackendException ex) {
      flash.addFlashAttribute("error", ex.safeMessage());
    }
    return "redirect:/admin/tags";
  }

  @GetMapping("/new")
  public String createPage(Model model) {
    if (!model.containsAttribute("tagForm")) {
      model.addAttribute("tagForm", new TagForm());
    }
    populateForm(model, null);
    return "admin/tags/form";
  }

  @PostMapping
  public String create(
    @Valid @ModelAttribute("tagForm") TagForm form,
    BindingResult binding,
    Model model,
    RedirectAttributes flash
  ) {
    if (binding.hasErrors()) {
      populateForm(model, null);
      return "admin/tags/form";
    }
    try {
      animals.createTag(new AnimalDtos.UpsertTagRequest(form.getName()));
      flash.addFlashAttribute("success", "Tag created.");
      return "redirect:/admin/tags";
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      populateForm(model, null);
      return "admin/tags/form";
    }
  }

  @GetMapping("/{id}/edit")
  public String editPage(@PathVariable UUID id, Model model) {
    if (!model.containsAttribute("tagForm")) {
      AnimalDtos.@org.jspecify.annotations.Nullable TagView existing = animals.findTag(id);
      if (existing == null) {
        return "redirect:/admin/tags";
      }
      TagForm f = new TagForm();
      f.setName(existing.name());
      model.addAttribute("tagForm", f);
    }
    populateForm(model, id);
    return "admin/tags/form";
  }

  @PostMapping("/{id}")
  public String update(
    @PathVariable UUID id,
    @Valid @ModelAttribute("tagForm") TagForm form,
    BindingResult binding,
    Model model,
    RedirectAttributes flash
  ) {
    if (binding.hasErrors()) {
      populateForm(model, id);
      return "admin/tags/form";
    }
    try {
      animals.updateTag(id, new AnimalDtos.UpsertTagRequest(form.getName()));
      flash.addFlashAttribute("success", "Tag updated.");
      return "redirect:/admin/tags";
    } catch (BackendException ex) {
      applyBackendErrors(binding, ex);
      populateForm(model, id);
      return "admin/tags/form";
    }
  }

  private static void populateForm(Model model, @org.jspecify.annotations.Nullable UUID id) {
    model.addAttribute("tagId", id);
    model.addAttribute("pageTitle", id == null ? "Admin \u00B7 New tag" : "Admin \u00B7 Edit tag");
    model.addAttribute("heading", id == null ? "New tag" : "Edit tag");
    model.addAttribute("submitLabel", id == null ? "Create tag" : "Save changes");
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
