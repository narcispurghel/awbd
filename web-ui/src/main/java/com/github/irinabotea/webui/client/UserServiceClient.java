package com.github.irinabotea.webui.client;

import com.github.irinabotea.webui.config.AppProperties;
import com.github.irinabotea.webui.security.JwtCookieService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Wraps REST calls to user-service (via api-gateway). */
@Service
public class UserServiceClient {

  private final RestClient http;
  private final JwtCookieService cookies;
  private final HttpServletRequest currentRequest;

  public UserServiceClient(
    AppProperties properties,
    JwtCookieService cookies,
    HttpServletRequest currentRequest
  ) {
    this.http = RestClient.builder()
      .baseUrl(properties.backend().baseUrl())
      .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .build();
    this.cookies = cookies;
    this.currentRequest = currentRequest;
  }

  public BackendDtos.RegisterResponse register(BackendDtos.RegisterRequest body) {
    return post("/api/v1/auth/register", body, BackendDtos.RegisterResponse.class, false);
  }

  public BackendDtos.LoginResponse login(BackendDtos.LoginRequest body) {
    return post("/api/v1/auth/login", body, BackendDtos.LoginResponse.class, false);
  }

  public void logout() {
    try {
      http.post().uri("/api/v1/auth/logout").headers(this::addAuth).retrieve().toBodilessEntity();
    } catch (RestClientResponseException ex) {
      throw translate(ex);
    }
  }

  public BackendDtos.CurrentUser getCurrentUser() {
    return get("/api/v1/users/me", BackendDtos.CurrentUser.class);
  }

  public BackendDtos.ProfileView updateProfile(BackendDtos.UpdateProfileRequest body) {
    return put("/api/v1/users/me/profile", body, BackendDtos.ProfileView.class);
  }

  public void changePassword(BackendDtos.ChangePasswordRequest body) {
    postVoid("/api/v1/users/me/password", body);
  }

  public void deactivate(BackendDtos.DeactivateRequest body) {
    postVoid("/api/v1/users/me/deactivate", body);
  }

  private <T> T get(String path, Class<T> type) {
    try {
      T result = http.get().uri(path).headers(this::addAuth).retrieve().body(type);
      return require(result);
    } catch (RestClientResponseException ex) {
      throw translate(ex);
    }
  }

  private <T> T post(String path, Object body, Class<T> type, boolean withAuth) {
    try {
      T result = http
        .post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(h -> {
          if (withAuth) addAuth(h);
        })
        .body(body)
        .retrieve()
        .body(type);
      return require(result);
    } catch (RestClientResponseException ex) {
      throw translate(ex);
    }
  }

  private void postVoid(String path, Object body) {
    try {
      http
        .post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(this::addAuth)
        .body(body)
        .retrieve()
        .toBodilessEntity();
    } catch (RestClientResponseException ex) {
      throw translate(ex);
    }
  }

  private <T> T put(String path, Object body, Class<T> type) {
    try {
      T result = http
        .put()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(this::addAuth)
        .body(body)
        .retrieve()
        .body(type);
      return require(result);
    } catch (RestClientResponseException ex) {
      throw translate(ex);
    }
  }

  private void addAuth(HttpHeaders headers) {
    String token = cookies.readToken(currentRequest);
    if (token != null) {
      headers.setBearerAuth(token);
    }
  }

  private BackendException translate(RestClientResponseException ex) {
    BackendDtos.@Nullable ApiError body = null;
    try {
      body = ex.getResponseBodyAs(BackendDtos.ApiError.class);
    } catch (Exception ignored) {
      // body wasn't JSON or didn't match shape; fall through with null
    }
    @Nullable
    String msg = body == null ? ex.getMessage() : body.message();
    @Nullable
    List<BackendDtos.FieldError> fields = body == null ? null : body.fieldErrors();
    return new BackendException(ex.getStatusCode().value(), msg, fields);
  }

  private static <T> T require(@Nullable T value) {
    if (value == null) {
      throw new BackendException(502, "Empty response from backend", null);
    }
    return value;
  }
}
