package com.legaldev.mvp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.compliance")
public record ComplianceUiProperties(boolean computeLineHint) {}
