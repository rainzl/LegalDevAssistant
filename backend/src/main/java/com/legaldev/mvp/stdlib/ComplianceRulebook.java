package com.legaldev.mvp.stdlib;

import java.util.ArrayList;
import java.util.List;

/** Compliance rules loaded from {@code compliance-rules.v0_1_0_demo.yaml} (stdlib-compliance). */
public final class ComplianceRulebook {

  private String libraryId;
  private String version;
  private String source;
  private final List<StdlibBootstrap.ComplianceRuleRaw> rules = new ArrayList<>();

  public void bind(StdlibBootstrap.ComplianceStdlibFile file) {
    this.libraryId = file.libraryId();
    this.version = file.version();
    this.source = file.source();
    this.rules.clear();
    this.rules.addAll(file.rules());
  }

  public String libraryId() {
    return libraryId;
  }

  public String version() {
    return version;
  }

  public String source() {
    return source;
  }

  public List<StdlibBootstrap.ComplianceRuleRaw> rules() {
    return rules;
  }

  public String libraryVersionRef() {
    return libraryId + "@v" + version;
  }
}
