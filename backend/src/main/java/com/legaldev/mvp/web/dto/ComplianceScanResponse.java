package com.legaldev.mvp.web.dto;

import java.util.List;

public record ComplianceScanResponse(List<ComplianceFinding> findings) {}
