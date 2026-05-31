package com.github.narcispurghel.common.jwt;

/** HTTP headers set by api-gateway after JWT validation. */
public final class GatewayHeaders {

  public static final String USER_ID = "X-User-Id";
  public static final String USER_EMAIL = "X-User-Email";
  public static final String USER_ROLES = "X-User-Roles";

  private GatewayHeaders() {}
}
