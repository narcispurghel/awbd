package com.github.narcispurghel.adoptionservice.config;

import com.github.narcispurghel.adoptionservice.security.GatewayIdentityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

  private final CorsConfigurationSource corsConfigurationSource;
  private final GatewayIdentityFilter gatewayIdentityFilter;

  SecurityConfig(
    CorsConfigurationSource corsConfigurationSource,
    GatewayIdentityFilter gatewayIdentityFilter
  ) {
    this.corsConfigurationSource = corsConfigurationSource;
    this.gatewayIdentityFilter = gatewayIdentityFilter;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
      .cors(cors -> cors.configurationSource(corsConfigurationSource))
      .csrf(AbstractHttpConfigurer::disable)
      .anonymous(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
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
      .build();
  }
}
