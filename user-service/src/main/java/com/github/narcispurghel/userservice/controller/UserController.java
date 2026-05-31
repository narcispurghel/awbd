package com.github.narcispurghel.userservice.controller;

import com.github.narcispurghel.userservice.dto.UserDtos;
import com.github.narcispurghel.userservice.service.UserAccountService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserAccountService userAccountService;

  public UserController(UserAccountService userAccountService) {
    this.userAccountService = userAccountService;
  }

  @GetMapping("/me")
  public UserDtos.CurrentUser me(Authentication authentication) {
    return userAccountService.currentUser(requireUserId(authentication));
  }

  @PutMapping("/me/profile")
  public UserDtos.ProfileView updateProfile(
    Authentication authentication,
    @Valid @RequestBody UserDtos.UpdateProfileRequest body
  ) {
    return userAccountService.updateProfile(requireUserId(authentication), body);
  }

  @PostMapping("/me/password")
  public void changePassword(
    Authentication authentication,
    @Valid @RequestBody UserDtos.ChangePasswordRequest body
  ) {
    userAccountService.changePassword(requireUserId(authentication), body);
  }

  @PostMapping("/me/deactivate")
  public void deactivate(
    Authentication authentication,
    @Valid @RequestBody UserDtos.DeactivateRequest body
  ) {
    userAccountService.deactivate(requireUserId(authentication), body);
  }

  private static UUID requireUserId(Authentication authentication) {
    return UUID.fromString(authentication.getName());
  }
}
