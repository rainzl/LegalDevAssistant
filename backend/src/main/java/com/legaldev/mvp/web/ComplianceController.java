package com.legaldev.mvp.web;

import com.legaldev.mvp.compliance.ComplianceScanService;
import com.legaldev.mvp.web.dto.ComplianceScanRequest;
import com.legaldev.mvp.web.dto.ComplianceScanResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ComplianceController {

  private final ComplianceScanService complianceScanService;
  private final ProcessingBoundary processingBoundary;

  public ComplianceController(
      ComplianceScanService complianceScanService, ProcessingBoundary processingBoundary) {
    this.complianceScanService = complianceScanService;
    this.processingBoundary = processingBoundary;
  }

  @PostMapping("/api/v1/compliance/scan")
  public ComplianceScanResponse scan(@Valid @RequestBody ComplianceScanRequest request) {
    return processingBoundary.complete(() -> complianceScanService.scan(request.source()));
  }
}
