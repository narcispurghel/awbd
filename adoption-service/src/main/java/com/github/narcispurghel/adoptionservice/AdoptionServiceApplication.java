package com.github.narcispurghel.adoptionservice;

import com.github.narcispurghel.common.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AdoptionServiceApplication {

  public static void main(String[] args) {
    DotenvLoader.load();
    SpringApplication.run(AdoptionServiceApplication.class, args);
  }
}
