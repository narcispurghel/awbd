package com.github.narcispurghel.animalservice.config;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(StorageProperties.class)
public class S3Config {

  private static final Logger log = LoggerFactory.getLogger(S3Config.class);

  @Bean
  public S3Client s3Client(StorageProperties props) {
    return S3Client.builder()
      .endpointOverride(URI.create(props.endpoint()))
      .region(Region.of(props.region()))
      .credentialsProvider(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create(props.accessKey(), props.secretKey())
        )
      )
      .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
      .build();
  }

  @Bean
  @ConditionalOnProperty(
    prefix = "storage.s3",
    name = "bootstrap-enabled",
    havingValue = "true",
    matchIfMissing = true
  )
  public ApplicationRunner bucketBootstrap(S3Client s3, StorageProperties props) {
    return args -> {
      try {
        s3.headBucket(HeadBucketRequest.builder().bucket(props.bucket()).build());
        log.info("Bucket '{}' already present", props.bucket());
      } catch (NoSuchBucketException ex) {
        try {
          s3.createBucket(CreateBucketRequest.builder().bucket(props.bucket()).build());
          log.info("Created bucket '{}'", props.bucket());
        } catch (BucketAlreadyOwnedByYouException ignored) {
          // raced with another instance — fine
        }
      } catch (S3Exception ex) {
        log.warn(
          "S3 bucket bootstrap failed (endpoint={} bucket={}): {}",
          props.endpoint(),
          props.bucket(),
          ex.awsErrorDetails() == null ? ex.getMessage() : ex.awsErrorDetails().errorMessage()
        );
      }
    };
  }
}
