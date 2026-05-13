package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.security.JwtCookieService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/error")
class ErrorPagesController {

    @GetMapping("/access-denied")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accessDenied() {
        return "error/access-denied";
    }
}

/** Translates BackendException into friendly pages and clears stale auth on 401. */
@ControllerAdvice
class GlobalExceptionHandler {

    private final JwtCookieService cookies;

    GlobalExceptionHandler(JwtCookieService cookies) {
        this.cookies = cookies;
    }

    @ExceptionHandler(BackendException.class)
    public ModelAndView onBackend(BackendException ex, HttpServletResponse response, Model model) {
        if (ex.isUnauthorized()) {
            cookies.clearCookie(response);
            SecurityContextHolder.clearContext();
            return new ModelAndView("redirect:/login");
        }
        ModelAndView mv = new ModelAndView("error/500");
        mv.setStatus(HttpStatus.BAD_GATEWAY);
        mv.addObject("message", ex.getMessage());
        return mv;
    }
}
