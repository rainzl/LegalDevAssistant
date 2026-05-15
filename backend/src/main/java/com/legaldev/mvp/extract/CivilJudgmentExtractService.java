package com.legaldev.mvp.extract;

import com.legaldev.mvp.stdlib.StdlibBootstrap;
import com.legaldev.mvp.web.dto.CivilJudgmentV1Extract;
import com.legaldev.mvp.web.dto.DocumentExtractRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Fixed pipeline {@code civil-judgment-v1} extractor. Marker/detection cues are aligned with demo
 * fixtures and {@code document-pipelines.v0_1_0_demo.yaml}; failures are contract error codes only.
 */
@Service
public class CivilJudgmentExtractService {

  private static final String PIPELINE_MISMATCH_MESSAGE =
      "输入正文缺少民事判决书管线所需的固定标记（演示：PIPELINE_MISMATCH）。";

  private static final Pattern CASE_NUMBER_LINE =
      Pattern.compile("（[0-9]{4}）[^\\n]*?号");
  private static final Pattern CAUSE =
      Pattern.compile("案由[：:]\\s*([^。\\n]+)");
  private static final Pattern JUDGEMENT_DATE =
      Pattern.compile("文书日期[：:]\\s*(\\d{4}-\\d{2}-\\d{2})");

  private final StdlibBootstrap stdlib;

  public CivilJudgmentExtractService(StdlibBootstrap stdlib) {
    this.stdlib = stdlib;
  }

  public CivilJudgmentV1Extract extract(DocumentExtractRequest request) {
    stdlib
        .pipelines()
        .find(request.pipelineId(), request.schemaVersion())
        .orElseThrow(
            () ->
                new ExtractBusinessException(
                    "SCHEMA_INTERNAL", "文书管线未在标准库注册表中找到（演示环境配置错误）。"));

    String content = request.content();
    if (content == null || !content.contains("民事判决书")) {
      throw new ExtractBusinessException("PIPELINE_MISMATCH", PIPELINE_MISMATCH_MESSAGE);
    }

    String court = firstCourtLine(content);
    String caseNo = firstMatch(CASE_NUMBER_LINE, content);
    String cause = firstGroup1(CAUSE, content);
    String date = firstGroup1(JUDGEMENT_DATE, content);

    if (court == null || court.isBlank() || caseNo == null || cause == null || cause.isBlank()) {
      throw new ExtractBusinessException(
          "INSUFFICIENT_MARKERS", "正文缺少民事判决书演示管线所需的关键字段标记（法院名称/案号/案由）。");
    }

    String causeTrim = cause.trim();
    if (causeTrim.length() > 200) {
      causeTrim = causeTrim.substring(0, 200);
    }
    String courtTrim = court.trim();
    if (courtTrim.length() > 120) {
      courtTrim = courtTrim.substring(0, 120);
    }

    return new CivilJudgmentV1Extract(
        request.pipelineId(),
        request.schemaVersion(),
        courtTrim,
        caseNo.trim(),
        date != null && !date.isBlank() ? date.trim() : null,
        causeTrim);
  }

  private static String firstCourtLine(String content) {
    for (String line : content.split("\\R")) {
      String t = line.trim();
      if (!t.isEmpty() && t.contains("人民法院")) {
        return t;
      }
    }
    return null;
  }

  private static String firstMatch(Pattern pattern, String content) {
    Matcher m = pattern.matcher(content);
    return m.find() ? m.group() : null;
  }

  private static String firstGroup1(Pattern pattern, String content) {
    Matcher m = pattern.matcher(content);
    return m.find() ? m.group(1) : null;
  }
}
