package com.legaldev.mvp.web;

import com.legaldev.mvp.caseno.CaseNumberService;
import com.legaldev.mvp.web.dto.CaseNumberValidateRequest;
import com.legaldev.mvp.web.dto.CaseNumberValidateResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CaseNumberController {

  private final CaseNumberService caseNumberService;
  private final ProcessingBoundary processingBoundary;

  public CaseNumberController(CaseNumberService caseNumberService, ProcessingBoundary processingBoundary) {
    this.caseNumberService = caseNumberService;
    this.processingBoundary = processingBoundary;
  }

  @PostMapping("/api/v1/case-number/validate")
  public CaseNumberValidateResponse validate(@Valid @RequestBody CaseNumberValidateRequest request) {
    return processingBoundary.complete(() -> caseNumberService.validate(request.candidate()));
  }
}
