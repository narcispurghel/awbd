package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.AdoptionServiceClient;
import com.github.irinabotea.webui.client.BackendException;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/adoptions")
public class AdoptionController {

  private final AdoptionServiceClient adoptions;

  public AdoptionController(AdoptionServiceClient adoptions) {
    this.adoptions = adoptions;
  }

  @PostMapping("/request/{animalId}")
  public String request(@PathVariable UUID animalId, RedirectAttributes flash) {
    try {
      adoptions.requestAdoption(animalId);
      flash.addFlashAttribute("success", "Adoption request submitted.");
      return "redirect:/adoptions/me";
    } catch (BackendException ex) {
      flash.addFlashAttribute("error", ex.safeMessage());
      return "redirect:/animals/" + animalId;
    }
  }
}
