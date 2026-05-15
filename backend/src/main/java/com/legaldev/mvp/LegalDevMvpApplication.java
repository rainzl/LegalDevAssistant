package com.legaldev.mvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LegalDevMvpApplication {

  public static void main(String[] args) {
    SpringApplication.run(LegalDevMvpApplication.class, args);
  }
}
