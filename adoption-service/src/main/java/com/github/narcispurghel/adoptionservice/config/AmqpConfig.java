package com.github.narcispurghel.adoptionservice.config;

import com.github.narcispurghel.adoptionservice.messaging.AdoptionBrokerEventPublisher;
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
public class AmqpConfig {

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
    return new TopicExchange(AdoptionBrokerEventPublisher.EXCHANGE_NAME);
  }
}
