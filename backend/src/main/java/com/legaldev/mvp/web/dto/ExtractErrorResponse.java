package com.legaldev.mvp.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExtractErrorResponse(String errorCode, String message, List<RuleRef> ruleRefs) {}
