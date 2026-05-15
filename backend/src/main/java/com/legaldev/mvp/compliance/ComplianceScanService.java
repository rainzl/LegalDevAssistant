package com.legaldev.mvp.compliance;

import com.legaldev.mvp.config.ComplianceUiProperties;
import com.legaldev.mvp.stdlib.StdlibBootstrap;
import com.legaldev.mvp.web.dto.ComplianceFinding;
import com.legaldev.mvp.web.dto.ComplianceScanResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** Offline compliance scan driven by stdlib-compliance YAML (regex / literal_any engines). */
@Service
public class ComplianceScanService {

  private final StdlibBootstrap stdlib;
  private final ComplianceUiProperties complianceUiProperties;

  public ComplianceScanService(StdlibBootstrap stdlib, ComplianceUiProperties complianceUiProperties) {
    this.stdlib = stdlib;
    this.complianceUiProperties = complianceUiProperties;
  }

  public ComplianceScanResponse scan(String source) {
    String text = source == null ? "" : source;
    var book = stdlib.compliance();
    List<ComplianceFinding> findings = new ArrayList<>();

    for (StdlibBootstrap.ComplianceRuleRaw rule : book.rules()) {
      if ("regex".equals(rule.engine()) && rule.pattern() != null) {
        Pattern p = Pattern.compile(rule.pattern());
        Matcher m = p.matcher(text);
        if (m.find()) {
          findings.add(toFinding(rule, text, m.start()));
        }
      } else if ("literal_any".equals(rule.engine()) && rule.tokens() != null) {
        for (String token : rule.tokens()) {
          if (token != null && text.contains(token)) {
            findings.add(toFinding(rule, text, text.indexOf(token)));
            break;
          }
        }
      }
    }
    return new ComplianceScanResponse(findings);
  }

  private ComplianceFinding toFinding(StdlibBootstrap.ComplianceRuleRaw rule, String text, int index) {
    Integer lineHint = null;
    if (complianceUiProperties.computeLineHint()) {
      lineHint = lineAtOffset(text, index);
    }
    return new ComplianceFinding(
        rule.ruleId(), rule.severity(), rule.messageZh(), lineHint, null);
  }

  private static int lineAtOffset(String text, int index) {
    if (index < 0) {
      return 1;
    }
    int line = 1;
    for (int i = 0; i < index && i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        line++;
      }
    }
    return line;
  }
}
