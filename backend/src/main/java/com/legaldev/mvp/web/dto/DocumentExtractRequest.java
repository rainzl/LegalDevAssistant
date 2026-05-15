package com.legaldev.mvp.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocumentExtractRequest(
    @NotBlank String pipelineId,
    @NotBlank String schemaVersion,
    @NotBlank String content,
    @NotNull String contentType,
    String encoding) {}
