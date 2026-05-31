package com.github.narcispurghel.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
class DaoAuthProviderConfig {

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;

  public DaoAuthProviderConfig(
    UserDetailsService userDetailsService,
    PasswordEncoder passwordEncoder
  ) {
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
  }

  @Bean
  DaoAuthenticationProvider authenticationProvider() {
    var provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }
}
