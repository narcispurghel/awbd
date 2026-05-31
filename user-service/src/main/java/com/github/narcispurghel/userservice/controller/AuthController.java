package com.github.narcispurghel.userservice.controller;

import com.github.narcispurghel.userservice.dto.AuthDtos;
import com.github.narcispurghel.userservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public AuthDtos.RegisterResponse register(@Valid @RequestBody AuthDtos.RegisterRequest body) {
    return authService.register(body);
  }

  @PostMapping("/login")
  public AuthDtos.LoginResponse login(@Valid @RequestBody AuthDtos.LoginRequest body) {
    return authService.login(body);
  }

  @PostMapping("/logout")
  public AuthDtos.LogoutResponse logout(HttpServletRequest request) {
    authService.logout(extractBearer(request));
    return new AuthDtos.LogoutResponse("logged out");
  }

  private static String extractBearer(HttpServletRequest request) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return "";
    }
    return authHeader.substring(7);
  }
}
