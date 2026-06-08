package com.github.narcispurghel.adoptionservice.messaging;

import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
class RabbitTemplateTestConfiguration {

  @Bean
  RabbitTemplate rabbitTemplate() {
    return Mockito.mock(RabbitTemplate.class);
  }
}
