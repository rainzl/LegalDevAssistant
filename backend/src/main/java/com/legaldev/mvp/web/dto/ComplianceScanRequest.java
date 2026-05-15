package com.legaldev.mvp.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ComplianceScanRequest(@NotBlank String source, String label) {}
