package com.github.narcispurghel.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import static java.lang.IO.println;

@Configuration(proxyBeanMethods = false)
class AuthManagerConfig {

  private final DaoAuthenticationProvider daoAuthenticationProvider;

  public AuthManagerConfig(DaoAuthenticationProvider daoAuthenticationProvider) {
    println("DAO Authentication Provider: " + daoAuthenticationProvider.getClass().getName());
    this.daoAuthenticationProvider = daoAuthenticationProvider;
  }

  @Bean
  AuthenticationManager authenticationManager() {
    return new ProviderManager(daoAuthenticationProvider);
  }
}
