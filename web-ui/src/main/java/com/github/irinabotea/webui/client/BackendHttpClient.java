package com.github.irinabotea.webui.client;

import com.github.irinabotea.webui.config.AppProperties;
import com.github.irinabotea.webui.security.JwtCookieService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Shared RestClient wrapper: attaches the JWT bearer from the auth cookie and translates errors. */
@Component
public class BackendHttpClient {

  private final RestClient http;
  private final JwtCookieService cookies;
  private final HttpServletRequest currentRequest;

  public BackendHttpClient(
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

  public <T> T get(String path, Class<T> type) {
    try {
      T result = http.get().uri(path).headers(this::addAuth).retrieve().body(type);
      return require(result);
    } catch (RestClientResponseException ex) {
      throw translate(ex);
    }
  }

  public <T> T get(String path, ParameterizedTypeReference<T> type) {
    try {
      T result = http.get().uri(path).headers(this::addAuth).retrieve().body(type);
      return require(result);
    } catch (RestClientResponseException ex) {
      throw translate(ex);
    }
  }

  public <T> T post(String path, Object body, Class<T> type) {
    return doPost(path, body, type, true);
  }

  public <T> T postNoAuth(String path, Object body, Class<T> type) {
    return doPost(path, body, type, false);
  }

  public void postVoid(String path, @Nullable Object body) {
    try {
      RestClient.RequestBodySpec spec = http.post().uri(path).headers(this::addAuth);
      if (body == null) {
        spec.retrieve().toBodilessEntity();
      } else {
        spec.contentType(MediaType.APPLICATION_JSON).body(body).retrieve().toBodilessEntity();
      }
    } catch (RestClientResponseException ex) {
      throw translate(ex);
    }
  }

  public <T> T put(String path, Object body, Class<T> type) {
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

  private <T> T doPost(String path, Object body, Class<T> type, boolean withAuth) {
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
