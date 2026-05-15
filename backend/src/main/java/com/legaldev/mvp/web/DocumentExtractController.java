package com.legaldev.mvp.web;

import com.legaldev.mvp.extract.CivilJudgmentExtractService;
import com.legaldev.mvp.web.dto.DocumentExtractRequest;
import com.legaldev.mvp.web.dto.DocumentExtractResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class DocumentExtractController {

  private final CivilJudgmentExtractService extractService;
  private final ProcessingBoundary processingBoundary;

  public DocumentExtractController(
      CivilJudgmentExtractService extractService, ProcessingBoundary processingBoundary) {
    this.extractService = extractService;
    this.processingBoundary = processingBoundary;
  }

  @PostMapping("/api/v1/document/extract")
  @ResponseStatus(HttpStatus.OK)
  public DocumentExtractResponse extract(@Valid @RequestBody DocumentExtractRequest request) {
    validateFrozenRequest(request);
    return processingBoundary.complete(() -> new DocumentExtractResponse(extractService.extract(request)));
  }

  private static void validateFrozenRequest(DocumentExtractRequest r) {
    if (!"civil-judgment-v1".equals(r.pipelineId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "pipelineId must be civil-judgment-v1 for MVP");
    }
    if (!"2026-05-13".equals(r.schemaVersion())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "schemaVersion must be 2026-05-13");
    }
    if (!"text/plain".equals(r.contentType())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contentType must be text/plain");
    }
    String enc = r.encoding() == null ? "UTF-8" : r.encoding();
    if (!"UTF-8".equals(enc)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "encoding must be UTF-8");
    }
  }
}
