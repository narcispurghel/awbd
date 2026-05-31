package com.github.irinabotea.webui.security;

import com.github.narcispurghel.common.jwt.JwtClaims;
import com.github.narcispurghel.common.jwt.JwtSupport;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads the JWT from the auth cookie on each request, validates it, and
 * populates the SecurityContext with a {@link UsernamePasswordAuthenticationToken}.
 */
@Component
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

  private final JwtCookieService cookies;

  public JwtCookieAuthenticationFilter(JwtCookieService cookies) {
    this.cookies = cookies;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain chain
  ) throws ServletException, IOException {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      String token = cookies.readToken(request);
      if (token != null) {
        Claims claims = cookies.parse(token);
        if (claims != null) {
          String principal = claims.getSubject();
          Object emailClaim = claims.get(JwtClaims.EMAIL);
          if (emailClaim != null && !emailClaim.toString().isBlank()) {
            principal = emailClaim.toString();
          }
          UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            principal,
            token,
            toAuthorities(claims)
          );
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      }
    }
    chain.doFilter(request, response);
  }

  private static List<SimpleGrantedAuthority> toAuthorities(Claims claims) {
    Object raw = claims.get(JwtClaims.ROLES);
    List<String> roles = new ArrayList<>();
    if (raw instanceof List<?> list) {
      for (Object item : list) {
        if (item != null) {
          roles.add(item.toString());
        }
      }
    }
    return JwtSupport.rolesFromHeader(String.join(",", roles))
      .stream()
      .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
      .map(SimpleGrantedAuthority::new)
      .toList();
  }
}
