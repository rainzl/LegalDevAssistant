package com.legaldev.mvp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.stdlib")
public record StdlibProperties(String indexClasspath) {}
