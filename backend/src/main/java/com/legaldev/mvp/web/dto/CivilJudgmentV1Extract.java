package com.legaldev.mvp.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CivilJudgmentV1Extract(
    String pipelineId,
    String schemaVersion,
    String courtNameSnippet,
    String caseNumberRaw,
    String judgementDateISO,
    String causeSnippet) {}
