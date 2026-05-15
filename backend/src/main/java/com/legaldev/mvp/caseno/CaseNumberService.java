package com.legaldev.mvp.caseno;

import com.legaldev.mvp.stdlib.StdlibBootstrap;
import com.legaldev.mvp.web.dto.CaseNumberValidateResponse;
import com.legaldev.mvp.web.dto.RuleRef;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** Case-number validation driven by stdlib-case-number YAML (no hardcoded national enumerations). */
@Service
public class CaseNumberService {

  private final StdlibBootstrap stdlib;

  public CaseNumberService(StdlibBootstrap stdlib) {
    this.stdlib = stdlib;
  }

  public CaseNumberValidateResponse validate(String candidateRaw) {
    String candidate = candidateRaw == null ? "" : candidateRaw.trim();
    var book = stdlib.caseNumbers();
    String libraryVersion = book.libraryVersionRef();

    StdlibBootstrap.CaseNumberRuleRaw junk = findByKind(book, "synthetic_junk_classifier");
    if (junk != null && junk.demoPattern() != null) {
      if (Pattern.compile(junk.demoPattern()).matcher(candidate).matches()) {
        return invalid(candidate, junk.id(), summarize(junk), libraryVersion);
      }
    }

    StdlibBootstrap.CaseNumberRuleRaw missingYear = findByKind(book, "synthetic_reject_missing_token");
    Objects.requireNonNull(missingYear, "stdlib-case-number missing synthetic_reject_missing_token rule");
    Pattern yearToken =
        Pattern.compile(
            Objects.requireNonNull(
                missingYear.requiredTokenPattern(), "CN-YEAR-SP-404 requires requiredTokenPattern"));
    boolean hasYear = yearToken.matcher(candidate).find();

    StdlibBootstrap.CaseNumberRuleRaw accept = findByKind(book, "synthetic_accept_pattern");
    Objects.requireNonNull(accept, "stdlib-case-number missing synthetic_accept_pattern rule");
    Pattern acceptPattern = Pattern.compile(Objects.requireNonNull(accept.demoPattern()));

    if (!hasYear) {
      return invalid(candidate, missingYear.id(), summarize(missingYear), libraryVersion);
    }

    if (acceptPattern.matcher(candidate).matches()) {
      return new CaseNumberValidateResponse(
          true, candidate, null, null, List.of(new RuleRef(accept.id(), libraryVersion)));
    }

    // Year present but demo accept regex did not match — keep stdlib rule ids only (fixtures focus on 404 vs junk).
    return invalid(candidate, missingYear.id(), summarize(accept), libraryVersion);
  }

  private static StdlibBootstrap.CaseNumberRuleRaw findByKind(
      com.legaldev.mvp.stdlib.CaseNumberRulebook book, String kind) {
    return book.rules().stream().filter(r -> kind.equals(r.kind())).findFirst().orElse(null);
  }

  private static String summarize(StdlibBootstrap.CaseNumberRuleRaw rule) {
    return rule.summaryZh() != null ? rule.summaryZh().trim() : "";
  }

  private static CaseNumberValidateResponse invalid(
      String candidate, String reasonCode, String message, String libraryVersion) {
    String msg = message == null || message.isBlank() ? "案号未通过演示校验。" : message;
    return new CaseNumberValidateResponse(
        false, null, reasonCode, msg, List.of(new RuleRef(reasonCode, libraryVersion)));
  }
}
