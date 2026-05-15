package com.legaldev.mvp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

  /**
   * MVC / JSON responses must use JSON {@link ObjectMapper}. A YAML-only mapper was previously the
   * sole bean and Spring wired it into {@code MappingJackson2HttpMessageConverter}, emitting YAML
   * bodies while still advertising {@code application/json} — browsers/axios then saw a string body
   * and {@code valid} disappeared on the client.
   */
  @Bean
  @Primary
  ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  @Qualifier("yamlObjectMapper")
  ObjectMapper yamlObjectMapper() {
    return new ObjectMapper(new YAMLFactory());
  }
}
