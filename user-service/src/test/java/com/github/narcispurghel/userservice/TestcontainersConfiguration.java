package com.github.narcispurghel.userservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

  private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
    DockerImageName.parse("postgres:17")
  );

  private static final GenericContainer<?> REDIS = new GenericContainer<>(
    DockerImageName.parse("redis:7-alpine")
  ).withExposedPorts(6379);

  static {
    POSTGRES.start();
    REDIS.start();
  }

  @Bean
  @ServiceConnection
  PostgreSQLContainer<?> postgresContainer() {
    return POSTGRES;
  }

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    registry.add("jwt.secret", () -> "test-secret-test-secret-test-secret-test-secre");
  }
}
