package com.github.narcispurghel.animalservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage.s3")
public record StorageProperties(
  String endpoint,
  String region,
  String accessKey,
  String secretKey,
  String bucket
) {}
