package com.github.irinabotea.webui.client;

import org.springframework.stereotype.Service;

/** Wraps REST calls to user-service (via api-gateway). */
@Service
public class UserServiceClient {

  private final BackendHttpClient http;

  public UserServiceClient(BackendHttpClient http) {
    this.http = http;
  }

  public BackendDtos.RegisterResponse register(BackendDtos.RegisterRequest body) {
    return http.postNoAuth("/api/v1/auth/register", body, BackendDtos.RegisterResponse.class);
  }

  public BackendDtos.LoginResponse login(BackendDtos.LoginRequest body) {
    return http.postNoAuth("/api/v1/auth/login", body, BackendDtos.LoginResponse.class);
  }

  public void logout() {
    http.postVoid("/api/v1/auth/logout", null);
  }

  public BackendDtos.CurrentUser getCurrentUser() {
    return http.get("/api/v1/users/me", BackendDtos.CurrentUser.class);
  }

  public BackendDtos.ProfileView updateProfile(BackendDtos.UpdateProfileRequest body) {
    return http.put("/api/v1/users/me/profile", body, BackendDtos.ProfileView.class);
  }

  public void changePassword(BackendDtos.ChangePasswordRequest body) {
    http.postVoid("/api/v1/users/me/password", body);
  }

  public void deactivate(BackendDtos.DeactivateRequest body) {
    http.postVoid("/api/v1/users/me/deactivate", body);
  }
}
