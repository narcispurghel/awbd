package com.github.irinabotea.webui.client;

import org.springframework.stereotype.Service;

/**
 * REST client for adoption-service (adoption requests, status, review actions).
 * Methods are added incrementally per the UI plan; currently only wires the shared HTTP client.
 */
@Service
public class AdoptionServiceClient {

  @SuppressWarnings("unused")
  private final BackendHttpClient http;

  public AdoptionServiceClient(BackendHttpClient http) {
    this.http = http;
  }
}
