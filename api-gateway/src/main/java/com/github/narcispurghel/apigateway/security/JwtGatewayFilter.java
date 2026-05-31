package com.github.narcispurghel.apigateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.narcispurghel.apigateway.dto.ApiError;
import com.github.narcispurghel.common.jwt.GatewayHeaders;
import com.github.narcispurghel.common.jwt.JwtSupport;
import com.github.narcispurghel.common.jwt.JwtSupport.InvalidJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtGatewayFilter extends OncePerRequestFilter {

  private static final String API_PREFIX = "/api/v1/";

  private final JwtSupport jwtSupport;
  private final GatewayTokenBlacklistService blacklistService;
  private final ObjectMapper objectMapper;

  public JwtGatewayFilter(
    JwtSupport jwtSupport,
    GatewayTokenBlacklistService blacklistService,
    ObjectMapper objectMapper
  ) {
    this.jwtSupport = jwtSupport;
    this.blacklistService = blacklistService;
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return !path.startsWith(API_PREFIX) || isPublicAuth(request, path);
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String token = extractBearer(request);
    if (token == null) {
      this.unauthorized(response, "Missing bearer token");
      return;
    }
    if (blacklistService.isBlacklisted(token)) {
      this.unauthorized(response, "Token has been revoked");
      return;
    }
    try {
      jwtSupport.validateToken(token);
      MutableHeaderHttpServletRequest wrapped = new MutableHeaderHttpServletRequest(request);
      wrapped.setHeader(GatewayHeaders.USER_ID, jwtSupport.subject(token));
      wrapped.setHeader(GatewayHeaders.USER_EMAIL, jwtSupport.email(token));
      wrapped.setHeader(
        GatewayHeaders.USER_ROLES,
        JwtSupport.rolesHeaderValue(jwtSupport.roles(token))
      );
      filterChain.doFilter(wrapped, response);
    } catch (InvalidJwtException ex) {
      String message = ex.getMessage() == null ? "Invalid JWT" : ex.getMessage();
      this.unauthorized(response, message);
    }
  }

  private static boolean isPublicAuth(HttpServletRequest request, String path) {
    if (!HttpMethod.POST.matches(request.getMethod())) {
      return false;
    }
    return path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register");
  }

  private static @Nullable String extractBearer(HttpServletRequest request) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return null;
    }
    return authHeader.substring(7);
  }

  private void unauthorized(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(
      response.getOutputStream(),
      ApiError.of(401, "Unauthorized", message, null)
    );
  }
}
