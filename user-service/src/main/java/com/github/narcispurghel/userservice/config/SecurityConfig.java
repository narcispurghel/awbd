package com.github.narcispurghel.userservice.config;

import com.github.narcispurghel.userservice.security.GatewayIdentityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
class SecurityConfig {

  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

  private final AuthenticationManager authenticationManager;
  private final CorsConfigurationSource corsConfigurationSource;
  private final GatewayIdentityFilter gatewayIdentityFilter;

  SecurityConfig(
    AuthenticationManager authenticationManager,
    CorsConfigurationSource corsConfigurationSource,
    GatewayIdentityFilter gatewayIdentityFilter
  ) {
    this.authenticationManager = authenticationManager;
    this.corsConfigurationSource = corsConfigurationSource;
    this.gatewayIdentityFilter = gatewayIdentityFilter;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) {
    return http
      .cors(cors -> cors.configurationSource(corsConfigurationSource))
      .csrf(AbstractHttpConfigurer::disable)
      .anonymous(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(authorize ->
        authorize
          .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/register")
          .permitAll()
          .anyRequest()
          .authenticated()
      )
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .addFilterBefore(gatewayIdentityFilter, UsernamePasswordAuthenticationFilter.class)
      .exceptionHandling(exConfigurer ->
        exConfigurer
          .accessDeniedHandler((_, res, ex) -> {
            log.debug(ex.getMessage());
            res.sendError(403, ex.getMessage());
          })
          .authenticationEntryPoint((_, res, ex) -> {
            log.debug(ex.getMessage());
            res.sendError(401, ex.getMessage());
          })
      )
      .authenticationManager(authenticationManager)
      .build();
  }
}
