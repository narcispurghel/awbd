package com.github.irinabotea.webui.client;

import org.springframework.stereotype.Service;

/**
 * REST client for animal-service (animals, shelters, species, breeds, medical records).
 * Methods are added incrementally per the UI plan; currently only wires the shared HTTP client.
 */
@Service
public class AnimalServiceClient {

  @SuppressWarnings("unused")
  private final BackendHttpClient http;

  public AnimalServiceClient(BackendHttpClient http) {
    this.http = http;
  }
}
