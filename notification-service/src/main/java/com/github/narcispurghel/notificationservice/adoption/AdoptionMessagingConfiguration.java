package com.github.narcispurghel.notificationservice.adoption;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
  prefix = "notification.messaging",
  name = "enabled",
  havingValue = "true",
  matchIfMissing = true
)
public class AdoptionMessagingConfiguration {

  public static final String QUEUE_NAME = "adoption.lifecycle-events";

  @Bean
  Jackson2JsonMessageConverter jackson2JsonMessageConverter(
    Jackson2ObjectMapperBuilder objectMapperBuilder
  ) {
    Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(
      objectMapperBuilder.build()
    );
    DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
    typeMapper.setTrustedPackages("com.github.narcispurghel.common.adoption.event");
    converter.setJavaTypeMapper(typeMapper);
    return converter;
  }

  @Bean
  TopicExchange adoptionLifecycleExchange() {
    return new TopicExchange("adoption.lifecycle.events");
  }

  @Bean
  Queue adoptionLifecycleQueue() {
    return new Queue(QUEUE_NAME, true);
  }

  @Bean
  Binding adoptionLifecycleBinding(Queue adoptionLifecycleQueue, TopicExchange exchange) {
    return BindingBuilder.bind(adoptionLifecycleQueue).to(exchange).with("adoption.lifecycle.*");
  }
}
