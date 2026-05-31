package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.BackendDtos;
import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.client.UserServiceClient;
import com.github.irinabotea.webui.web.form.ProfileForm;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

  private final UserServiceClient client;

  public ProfileController(UserServiceClient client) {
    this.client = client;
  }

  @GetMapping
  public String view(Model model) {
    BackendDtos.CurrentUser me = client.getCurrentUser();
    model.addAttribute("user", me);
    return "profile/view";
  }

  @GetMapping("/edit")
  public String editPage(Model model) {
    if (!model.containsAttribute("profileForm")) {
      BackendDtos.CurrentUser me = client.getCurrentUser();
      model.addAttribute("profileForm", toForm(me.profile()));
      model.addAttribute("user", me);
    }
    return "profile/edit";
  }

  @PostMapping("/edit")
  public String update(
    @Valid @ModelAttribute("profileForm") ProfileForm form,
    BindingResult binding,
    RedirectAttributes flash,
    Model model
  ) {
    if (binding.hasErrors()) {
      model.addAttribute("user", client.getCurrentUser());
      return "profile/edit";
    }
    try {
      client.updateProfile(
        new BackendDtos.UpdateProfileRequest(
          form.getFirstName(),
          form.getLastName(),
          blankToNull(form.getPhone()),
          blankToNull(form.getCity()),
          form.getHouseType(),
          form.isHasYard(),
          form.getExperienceWithPets()
        )
      );
      flash.addFlashAttribute("success", "Profile saved.");
      return "redirect:/profile";
    } catch (BackendException ex) {
      for (BackendDtos.FieldError fe : ex.fieldErrors()) {
        binding.rejectValue(fe.field(), "backend", fe.message());
      }
      if (ex.fieldErrors().isEmpty()) {
        binding.reject("backend", ex.safeMessage());
      }
      model.addAttribute("user", client.getCurrentUser());
      return "profile/edit";
    }
  }

  private static ProfileForm toForm(BackendDtos.@Nullable ProfileView v) {
    ProfileForm f = new ProfileForm();
    if (v == null) {
      return f;
    }
    f.setFirstName(v.firstName());
    f.setLastName(v.lastName());
    f.setPhone(v.phone());
    f.setCity(v.city());
    f.setHouseType(v.houseType());
    f.setHasYard(v.hasYard());
    f.setExperienceWithPets(v.experienceWithPets());
    f.setVerifiedStatus(v.verifiedStatus());
    return f;
  }

  private static @Nullable String blankToNull(@Nullable String s) {
    return (s == null || s.isBlank()) ? null : s;
  }
}
