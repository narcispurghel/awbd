package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.BackendDtos;
import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.client.UserServiceClient;
import com.github.irinabotea.webui.security.JwtCookieService;
import com.github.irinabotea.webui.web.form.LoginForm;
import com.github.irinabotea.webui.web.form.RegisterForm;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

  private final UserServiceClient client;
  private final JwtCookieService cookies;

  public AuthController(UserServiceClient client, JwtCookieService cookies) {
    this.client = client;
    this.cookies = cookies;
  }

  @GetMapping("/login")
  public String loginPage(@RequestParam(required = false) @Nullable String redirect, Model model) {
    if (!model.containsAttribute("loginForm")) {
      model.addAttribute("loginForm", new LoginForm());
    }
    model.addAttribute("redirect", redirect == null ? "/profile" : redirect);
    return "auth/login";
  }

  @PostMapping("/login")
  public String login(
    @Valid @ModelAttribute("loginForm") LoginForm form,
    BindingResult binding,
    @RequestParam(required = false) @Nullable String redirect,
    HttpServletResponse response,
    RedirectAttributes flash,
    Model model
  ) {
    String safeRedirect = redirect == null ? "/profile" : redirect;
    if (binding.hasErrors()) {
      model.addAttribute("redirect", safeRedirect);
      return "auth/login";
    }
    try {
      BackendDtos.LoginResponse resp = client.login(
        new BackendDtos.LoginRequest(form.getEmail(), form.getPassword())
      );
      cookies.writeCookie(response, resp.token(), resp.expiresInSeconds());
      return "redirect:" + safeRedirect;
    } catch (BackendException ex) {
      flash.addFlashAttribute(
        "error",
        ex.isUnauthorized() ? "Invalid email or password" : ex.getMessage()
      );
      return "redirect:/login?redirect=" + safeRedirect;
    }
  }

  @GetMapping("/register")
  public String registerPage(Model model) {
    if (!model.containsAttribute("registerForm")) {
      model.addAttribute("registerForm", new RegisterForm());
    }
    return "auth/register";
  }

  @PostMapping("/register")
  public String register(
    @Valid @ModelAttribute("registerForm") RegisterForm form,
    BindingResult binding,
    RedirectAttributes flash
  ) {
    if (binding.hasErrors()) {
      return "auth/register";
    }
    try {
      client.register(
        new BackendDtos.RegisterRequest(
          form.getEmail(),
          form.getPassword(),
          form.getFirstName(),
          form.getLastName()
        )
      );
      flash.addFlashAttribute("success", "Account created. Please log in.");
      return "redirect:/login";
    } catch (BackendException ex) {
      for (BackendDtos.FieldError fe : ex.fieldErrors()) {
        binding.rejectValue(fe.field(), "backend", fe.message());
      }
      if (ex.fieldErrors().isEmpty()) {
        binding.reject("backend", ex.safeMessage());
      }
      return "auth/register";
    }
  }

  @PostMapping("/logout")
  public String logout(HttpServletResponse response) {
    try {
      client.logout();
    } catch (BackendException ignored) {
      // Clear local session even if backend logout fails.
    }
    cookies.clearCookie(response);
    SecurityContextHolder.clearContext();
    return "redirect:/";
  }
}
