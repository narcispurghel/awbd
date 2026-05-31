package com.github.narcispurghel.apigateway.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.narcispurghel.common.jwt.JwtSupport;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class JwtGatewayFilterTest {

  private static final String SECRET = "test-secret-test-secret-test-secret-test-secre";

  private JwtSupport jwtSupport;
  private GatewayTokenBlacklistService blacklistService;
  private JwtGatewayFilter filter;

  @BeforeEach
  void setUp() {
    jwtSupport = new JwtSupport(SECRET);
    blacklistService = mock(GatewayTokenBlacklistService.class);
    filter = new JwtGatewayFilter(jwtSupport, blacklistService, new ObjectMapper());
  }

  @Test
  void rejectsMissingBearerOnProtectedPath() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    verify(chain, never()).doFilter(any(), any());
    org.junit.jupiter.api.Assertions.assertEquals(401, response.getStatus());
  }

  @Test
  void allowsPublicLoginPathWithoutToken() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  void forwardsIdentityHeadersForValidToken() throws Exception {
    String token = Jwts.builder()
      .subject("user-1")
      .claim("email", "user@example.com")
      .claim("roles", List.of("USER"))
      .issuedAt(Date.from(Instant.now()))
      .expiration(Date.from(Instant.now().plusSeconds(3600)))
      .signWith(jwtSupport.signingKey())
      .compact();

    when(blacklistService.isBlacklisted(token)).thenReturn(false);

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me");
    request.addHeader("Authorization", "Bearer " + token);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(any(), any());
  }
}
