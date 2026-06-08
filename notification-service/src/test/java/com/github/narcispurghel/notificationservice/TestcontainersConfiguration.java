package com.github.narcispurghel.notificationservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

  private static final RabbitMQContainer RABBITMQ = new RabbitMQContainer(
    DockerImageName.parse("rabbitmq:3-management")
  );

  static {
    RABBITMQ.start();
  }

  @Bean
  @ServiceConnection
  RabbitMQContainer rabbitmqContainer() {
    return RABBITMQ;
  }
}
