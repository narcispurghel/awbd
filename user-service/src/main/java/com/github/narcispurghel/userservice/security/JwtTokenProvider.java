package com.github.narcispurghel.userservice.security;

import com.github.narcispurghel.common.jwt.JwtClaims;
import com.github.narcispurghel.common.jwt.JwtSupport;
import com.github.narcispurghel.userservice.entity.User;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenProvider {

  private final JwtSupport jwtSupport;
  private final long jwtExpirationMs;

  public JwtTokenProvider(
    @Value("${jwt.secret}") String jwtSecret,
    @Value("${jwt.expiration-ms}") long jwtExpirationMs
  ) {
    this.jwtSupport = new JwtSupport(jwtSecret);
    this.jwtExpirationMs = jwtExpirationMs;
  }

  /**
   * Generates a JWT token for the given user.
   */
  public String generateToken(User user) {
    Instant now = Instant.now();
    Date issuedAt = Date.from(now);
    Date expiryDate = Date.from(now.plusMillis(jwtExpirationMs));
    String userId = user.getId().toString();
    List<String> roles = List.of(user.getRole().name());
    return io.jsonwebtoken.Jwts.builder()
      .subject(userId)
      .claim(JwtClaims.EMAIL, user.getEmail())
      .claim(JwtClaims.ROLES, roles)
      .issuedAt(issuedAt)
      .expiration(expiryDate)
      .signWith(jwtSupport.signingKey())
      .compact();
  }

  public void validateToken(String token) {
    jwtSupport.validateToken(token);
  }

  public Authentication buildAuthentication(String userId, String jwtToken) {
    List<SimpleGrantedAuthority> authorities = jwtSupport
      .roles(jwtToken)
      .stream()
      .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
      .map(SimpleGrantedAuthority::new)
      .toList();
    return new UsernamePasswordAuthenticationToken(userId, null, authorities);
  }

  public long remainingTtlSeconds(String token) {
    return jwtSupport.remainingTtlSeconds(token);
  }

  public long blacklistTtlSeconds(String token) {
    try {
      return remainingTtlSeconds(token);
    } catch (Exception _) {
      return Math.max(jwtExpirationMs / 1000, 1L);
    }
  }

  public long expirationSeconds() {
    return Math.max(jwtExpirationMs / 1000, 1L);
  }

  public JwtSupport jwtSupport() {
    return jwtSupport;
  }
}
