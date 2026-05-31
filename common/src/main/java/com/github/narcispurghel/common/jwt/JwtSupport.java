package com.github.narcispurghel.common.jwt;

import com.github.narcispurghel.common.AwbdCharsets;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.jspecify.annotations.Nullable;

/** Parse and validate HS256 JWTs with a shared secret. */
public final class JwtSupport {

  private final SecretKey signingKey;

  public JwtSupport(String secret) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(AwbdCharsets.UTF_8));
  }

  public SecretKey signingKey() {
    return signingKey;
  }

  public Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
  }

  public void validateToken(String token) {
    try {
      parseClaims(token);
    } catch (JwtException ex) {
      throw new InvalidJwtException("Invalid JWT", ex);
    }
    if (isExpired(token)) {
      throw new InvalidJwtException("Token has expired");
    }
  }

  public boolean isExpired(String token) {
    return parseClaims(token).getExpiration().before(new Date());
  }

  public String subject(String token) {
    return parseClaims(token).getSubject();
  }

  public String email(String token) {
    Object value = parseClaims(token).get(JwtClaims.EMAIL);
    return value == null ? "" : value.toString();
  }

  public List<String> roles(String token) {
    Object raw = parseClaims(token).get(JwtClaims.ROLES);
    if (raw instanceof List<?> list) {
      List<String> roles = new ArrayList<>();
      for (Object item : list) {
        if (item != null) {
          roles.add(item.toString());
        }
      }
      return roles;
    }
    return List.of();
  }

  public long remainingTtlSeconds(String token) {
    Date expiration = parseClaims(token).getExpiration();
    long seconds = Duration.between(Instant.now(), expiration.toInstant()).getSeconds();
    return Math.max(seconds, 1L);
  }

  public static String rolesHeaderValue(List<String> roles) {
    return String.join(",", roles);
  }

  public static List<String> rolesFromHeader(@Nullable String header) {
    if (header == null || header.isBlank()) {
      return List.of();
    }
    return List.of(header.split(","));
  }

  public static class InvalidJwtException extends RuntimeException {

    public InvalidJwtException(String message) {
      super(message);
    }

    public InvalidJwtException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
