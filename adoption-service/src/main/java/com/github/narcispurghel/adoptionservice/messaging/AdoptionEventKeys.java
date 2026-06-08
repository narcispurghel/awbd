package com.github.narcispurghel.adoptionservice.messaging;

import java.util.UUID;

public final class AdoptionEventKeys {

  public static final String ADOPTION_SUBMITTED = "adoption.lifecycle.submitted";
  public static final String ADOPTION_APPROVED = "adoption.lifecycle.approved";
  public static final String ADOPTION_REJECTED = "adoption.lifecycle.rejected";
  public static final String ADOPTION_CANCELLED = "adoption.lifecycle.cancelled";

  private AdoptionEventKeys() {}

  public static String eventKey(String eventType, UUID adoptionRequestId) {
    return eventType + ":" + adoptionRequestId;
  }
}
