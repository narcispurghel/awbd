package com.github.narcispurghel.userservice.security;

import com.github.narcispurghel.common.jwt.GatewayHeaders;
import com.github.narcispurghel.common.jwt.JwtSupport;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Trusts identity headers injected by api-gateway after JWT validation.
 */
@Component
public class GatewayIdentityFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    SecurityContextHolder.getContext().setAuthentication(authentication(request));
    filterChain.doFilter(request, response);
  }

  private @Nullable UsernamePasswordAuthenticationToken authentication(HttpServletRequest request) {
    String userId = request.getHeader(GatewayHeaders.USER_ID);
    if (userId == null || userId.isBlank()) {
      return null;
    }
    List<SimpleGrantedAuthority> authorities = JwtSupport.rolesFromHeader(
      request.getHeader(GatewayHeaders.USER_ROLES)
    )
      .stream()
      .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
      .map(SimpleGrantedAuthority::new)
      .toList();
    return new UsernamePasswordAuthenticationToken(userId, null, authorities);
  }
}
