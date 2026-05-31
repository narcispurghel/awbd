package com.github.narcispurghel.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.narcispurghel.common.jwt.JwtSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GatewayJwtConfig {

  @Bean
  JwtSupport jwtSupport(@Value("${jwt.secret}") String secret) {
    return new JwtSupport(secret);
  }

  @Bean
  ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
