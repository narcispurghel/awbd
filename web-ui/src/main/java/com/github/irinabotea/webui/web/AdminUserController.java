package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.BackendDtos;
import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.client.UserServiceClient;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

  private final UserServiceClient users;

  public AdminUserController(UserServiceClient users) {
    this.users = users;
  }

  @GetMapping("/{id}")
  public String view(@PathVariable UUID id, Model model, RedirectAttributes flash) {
    BackendDtos.CurrentUser user;
    try {
      user = users.getUserById(id);
    } catch (BackendException ex) {
      flash.addFlashAttribute("error", ex.safeMessage());
      return "redirect:/admin/adoptions";
    }
    model.addAttribute("user", user);
    return "admin/users/view";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable UUID id, RedirectAttributes flash) {
    try {
      users.deleteUser(id);
      flash.addFlashAttribute("success", "User deleted.");
    } catch (BackendException ex) {
      flash.addFlashAttribute("error", ex.safeMessage());
    }
    return "redirect:/admin/adoptions";
  }
}
