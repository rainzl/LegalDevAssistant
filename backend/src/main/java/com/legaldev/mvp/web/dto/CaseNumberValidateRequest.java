package com.legaldev.mvp.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CaseNumberValidateRequest(@NotBlank String candidate) {}
