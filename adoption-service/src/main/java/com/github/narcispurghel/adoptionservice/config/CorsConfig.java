package com.github.narcispurghel.adoptionservice.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
public class CorsConfig {

  private static final List<String> ALLOWED_HTTP_METHODS = List.of(
    "GET",
    "POST",
    "PUT",
    "PATCH",
    "DELETE"
  );

  private static final List<String> ALLOWED_HEADERS = List.of(
    "Content-Type",
    "Authorization",
    "Accept",
    "Origin",
    "X-User-Id",
    "X-User-Email",
    "X-User-Roles"
  );

  private final String allowedOrigins;

  public CorsConfig(@Value("${cors.allowed-origins}") String allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(parseOrigins(allowedOrigins));
    configuration.setAllowedHeaders(ALLOWED_HEADERS);
    configuration.setAllowedMethods(ALLOWED_HTTP_METHODS);
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  private List<String> parseOrigins(String originsString) {
    return Arrays.stream(originsString.split(",")).map(String::trim).toList();
  }
}
