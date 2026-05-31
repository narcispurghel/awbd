package com.github.irinabotea.webui.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Backend backend, Jwt jwt) {
  public record Backend(String baseUrl) {}

  public record Jwt(String secret, String cookieName, boolean cookieSecure) {}
}
