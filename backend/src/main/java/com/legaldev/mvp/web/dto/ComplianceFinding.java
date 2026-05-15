package com.legaldev.mvp.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ComplianceFinding(
    String ruleId,
    String severity,
    String message,
    Integer lineHint,
    String snippet) {}
