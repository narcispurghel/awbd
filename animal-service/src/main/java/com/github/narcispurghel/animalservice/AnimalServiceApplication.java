package com.github.narcispurghel.animalservice;

import com.github.narcispurghel.common.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnimalServiceApplication {

  public static void main(String[] args) {
    DotenvLoader.load();
    SpringApplication.run(AnimalServiceApplication.class, args);
  }
}
