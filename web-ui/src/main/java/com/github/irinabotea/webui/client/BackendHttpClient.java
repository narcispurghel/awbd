package com.github.irinabotea.webui.client;

import com.github.irinabotea.webui.config.AppProperties;
import com.github.irinabotea.webui.security.JwtCookieService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

  public void delete(String path) {
    try {
      http.delete().uri(path).headers(this::addAuth).retrieve().toBodilessEntity();
    } catch (RestClientResponseException ex) {
      throw translate(ex);
    }
  }

  public <T> T postMultipart(
    String path,
    String partName,
    String filename,
    String contentType,
    byte[] data,
    Class<T> type
  ) {
    MultiValueMap<String, HttpEntity<?>> parts = new LinkedMultiValueMap<>();
    HttpHeaders partHeaders = new HttpHeaders();
    partHeaders.setContentType(MediaType.parseMediaType(contentType));
    partHeaders.setContentDispositionFormData(partName, filename);
    ByteArrayResource resource = new ByteArrayResource(data) {
      @Override
      public String getFilename() {
        return filename;
      }
    };
    parts.add(partName, new HttpEntity<>(resource, partHeaders));
    try {
      T result = http
        .post()
        .uri(path)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .headers(this::addAuth)
        .body(parts)
        .retrieve()
        .body(type);
      return require(result);
    } catch (RestClientResponseException ex) {
      throw translate(ex);
    }
  }

  public BytesResponse getBytes(String path) {
    try {
      ResponseEntity<byte[]> entity = http
        .get()
        .uri(path)
        .headers(this::addAuth)
        .retrieve()
        .toEntity(byte[].class);
      MediaType ct = entity.getHeaders().getContentType();
      byte[] body = entity.getBody();
      return new BytesResponse(
        body == null ? new byte[0] : body,
        ct == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : ct.toString()
      );
    } catch (RestClientResponseException ex) {
      throw translate(ex);
    }
  }

  public record BytesResponse(byte[] bytes, String contentType) {}

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
