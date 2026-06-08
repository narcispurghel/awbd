package com.github.narcispurghel.notificationservice;

import com.github.narcispurghel.common.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationServiceApplication {

  public static void main(String[] args) {
    DotenvLoader.load();
    SpringApplication.run(NotificationServiceApplication.class, args);
  }
}
