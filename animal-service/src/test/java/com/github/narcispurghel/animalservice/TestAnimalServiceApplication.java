package com.github.narcispurghel.animalservice;

import org.springframework.boot.SpringApplication;

public class TestAnimalServiceApplication {

  public static void main(String[] args) {
    SpringApplication.from(AnimalServiceApplication::main)
      .with(TestcontainersConfiguration.class)
      .run(args);
  }
}
