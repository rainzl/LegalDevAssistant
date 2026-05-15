package com.legaldev.mvp.stdlib;

import java.util.ArrayList;
import java.util.List;

/** Case-number rules loaded from {@code rules-case-number-demo.yaml} (stdlib-case-number). */
public final class CaseNumberRulebook {

  private String libraryId;
  private String version;
  private String source;
  private final List<StdlibBootstrap.CaseNumberRuleRaw> rules = new ArrayList<>();

  public void bind(StdlibBootstrap.CaseNumberStdlibFile file) {
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

  public List<StdlibBootstrap.CaseNumberRuleRaw> rules() {
    return rules;
  }

  public String libraryVersionRef() {
    return libraryId + "@v" + version;
  }
}
