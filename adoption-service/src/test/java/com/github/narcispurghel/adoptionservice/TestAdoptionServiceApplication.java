package com.github.narcispurghel.adoptionservice;

import org.springframework.boot.SpringApplication;

public class TestAdoptionServiceApplication {

  public static void main(String[] args) {
    SpringApplication.from(AdoptionServiceApplication::main)
      .with(TestcontainersConfiguration.class)
      .run(args);
  }
}
