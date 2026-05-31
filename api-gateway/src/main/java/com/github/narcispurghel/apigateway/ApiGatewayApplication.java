package com.github.narcispurghel.apigateway;

import com.github.narcispurghel.common.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {

  public static void main(String[] args) {
    DotenvLoader.load();
    SpringApplication.run(ApiGatewayApplication.class, args);
  }
}
