package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.BackendDtos;
import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.client.UserServiceClient;
import com.github.irinabotea.webui.security.JwtCookieService;
import com.github.irinabotea.webui.web.form.ChangePasswordForm;
import com.github.irinabotea.webui.web.form.DeactivateForm;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserServiceClient client;
    private final JwtCookieService cookies;

    public AccountController(UserServiceClient client, JwtCookieService cookies) {
        this.client = client;
        this.cookies = cookies;
    }

    @GetMapping
    public String settings(Model model) {
        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }
        if (!model.containsAttribute("deactivateForm")) {
            model.addAttribute("deactivateForm", new DeactivateForm());
        }
        model.addAttribute("user", client.getCurrentUser());
        return "account/settings";
    }

    @PostMapping("/password")
    public String changePassword(
        @Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
        BindingResult binding,
        Model model,
        RedirectAttributes flash
    ) {
        if (binding.hasErrors()) {
            model.addAttribute("deactivateForm", new DeactivateForm());
            model.addAttribute("user", client.getCurrentUser());
            return "account/settings";
        }
        try {
            client.changePassword(new BackendDtos.ChangePasswordRequest(form.getCurrentPassword(), form.getNewPassword()));
            flash.addFlashAttribute("success", "Password changed.");
            return "redirect:/account";
        } catch (BackendException ex) {
            for (BackendDtos.FieldError fe : ex.fieldErrors()) {
                binding.rejectValue(fe.field(), "backend", fe.message());
            }
            if (ex.fieldErrors().isEmpty()) {
                binding.reject("backend", ex.safeMessage());
            }
            model.addAttribute("deactivateForm", new DeactivateForm());
            model.addAttribute("user", client.getCurrentUser());
            return "account/settings";
        }
    }

    @PostMapping("/deactivate")
    public String deactivate(
        @Valid @ModelAttribute("deactivateForm") DeactivateForm form,
        BindingResult binding,
        Model model,
        HttpServletResponse response,
        RedirectAttributes flash
    ) {
        if (binding.hasErrors()) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
            model.addAttribute("user", client.getCurrentUser());
            return "account/settings";
        }
        try {
            client.deactivate(new BackendDtos.DeactivateRequest(form.getPassword()));
            cookies.clearCookie(response);
            SecurityContextHolder.clearContext();
            flash.addFlashAttribute("success", "Account deactivated.");
            return "redirect:/";
        } catch (BackendException ex) {
            for (BackendDtos.FieldError fe : ex.fieldErrors()) {
                binding.rejectValue(fe.field(), "backend", fe.message());
            }
            if (ex.fieldErrors().isEmpty()) {
                binding.reject("backend", ex.safeMessage());
            }
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
            model.addAttribute("user", client.getCurrentUser());
            return "account/settings";
        }
    }
}
