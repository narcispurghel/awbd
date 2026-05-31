package com.github.narcispurghel.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
class ApiGatewayApplicationTests {

  private static final GenericContainer<?> REDIS = new GenericContainer<>(
    DockerImageName.parse("redis:7-alpine")
  ).withExposedPorts(6379);

  static {
    REDIS.start();
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    registry.add("jwt.secret", () -> "test-secret-test-secret-test-secret-test-secre");
    registry.add("eureka.client.enabled", () -> "false");
    registry.add("spring.cloud.discovery.enabled", () -> "false");
  }

  @Test
  void contextLoads() {}
}
