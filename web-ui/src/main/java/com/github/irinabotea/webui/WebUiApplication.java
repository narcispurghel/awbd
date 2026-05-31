package com.github.irinabotea.webui;

import com.github.narcispurghel.common.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WebUiApplication {

  public static void main(String[] args) {
    DotenvLoader.load();
    SpringApplication.run(WebUiApplication.class, args);
  }
}
