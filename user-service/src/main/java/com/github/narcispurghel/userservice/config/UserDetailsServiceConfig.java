package com.github.narcispurghel.userservice.config;

import com.github.narcispurghel.userservice.entity.User;
import com.github.narcispurghel.userservice.repository.UserJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration(proxyBeanMethods = false)
class UserDetailsServiceConfig {

  // TODO: replace hardcoded values with entity fields (accountExpired, credentialsExpired, accountLocked)
  @Bean
  UserDetailsService userDetailsService(UserJpaRepository ujr) {
    return username -> {
      User user = ujr
        .findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException(username));
      return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
        .accountExpired(false)
        .credentialsExpired(false)
        .accountLocked(false)
        .disabled(!user.isActive())
        .password(user.getPasswordHash())
        .roles(user.getRole().name())
        .build();
    };
  }
}
