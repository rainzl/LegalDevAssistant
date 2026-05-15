package com.legaldev.mvp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.processing")
public record AppProcessingProperties(int timeoutSeconds) {}
