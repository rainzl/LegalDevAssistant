package com.legaldev.mvp;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MvpGoldenFixturesIT {

  @Autowired private TestRestTemplate rest;
  @Autowired private ObjectMapper objectMapper;

  private static HttpEntity<String> jsonEntity(String jsonBody) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
    return new HttpEntity<>(jsonBody, headers);
  }

  /** Golden file may include {@code _meta} for window G trace; strip before comparing API fields. */
  private JsonNode goldenExpectedWithoutMeta(Path goldenExpectedPath) throws Exception {
    JsonNode root = objectMapper.readTree(Files.readString(goldenExpectedPath, StandardCharsets.UTF_8));
    if (root instanceof ObjectNode obj) {
      obj.remove("_meta");
    }
    return root;
  }

  private void assertCaseNumberBodyMatchesGolden(String responseBody, Path goldenExpectedPath) throws Exception {
    JsonNode expected = goldenExpectedWithoutMeta(goldenExpectedPath);
    JsonNode act = objectMapper.readTree(responseBody);

    assertThat(act.path("valid").asBoolean()).isEqualTo(expected.path("valid").asBoolean());

    if (expected.path("normalized").isNull() || expected.path("normalized").isMissingNode()) {
      assertThat(act.path("normalized").isNull() || act.path("normalized").isMissingNode()).isTrue();
    } else {
      assertThat(act.path("normalized").asText()).isEqualTo(expected.path("normalized").asText());
    }

    if (expected.path("reasonCode").isNull() || expected.path("reasonCode").isMissingNode()) {
      assertThat(act.path("reasonCode").isNull() || act.path("reasonCode").isMissingNode()).isTrue();
    } else {
      assertThat(act.path("reasonCode").asText()).isEqualTo(expected.path("reasonCode").asText());
    }

    if (expected.path("message").isNull() || expected.path("message").isMissingNode()) {
      assertThat(act.path("message").isNull() || act.path("message").isMissingNode()).isTrue();
    } else {
      assertThat(act.path("message").asText()).isEqualTo(expected.path("message").asText());
    }

    assertThat(act.path("ruleRefs").isArray()).isTrue();
    assertThat(act.path("ruleRefs").size()).isGreaterThan(0);
    assertThat(act.path("ruleRefs").get(0).path("ruleId").asText())
        .isEqualTo(expected.path("ruleRefs").get(0).path("ruleId").asText());
    assertThat(act.path("ruleRefs").get(0).path("libraryVersion").asText())
        .isEqualTo(expected.path("ruleRefs").get(0).path("libraryVersion").asText());
  }

  private void assertComplianceFindingsMatchGolden(String responseBody, Path goldenExpectedPath) throws Exception {
    JsonNode expected = goldenExpectedWithoutMeta(goldenExpectedPath);
    JsonNode act = objectMapper.readTree(responseBody);
    JsonNode expFindings = expected.path("findings");
    JsonNode actFindings = act.path("findings");
    assertThat(actFindings.isArray()).isTrue();
    assertThat(actFindings.size()).isEqualTo(expFindings.size());

    for (int i = 0; i < expFindings.size(); i++) {
      JsonNode ef = expFindings.get(i);
      JsonNode af = actFindings.get(i);
      assertThat(af.path("ruleId").asText()).isEqualTo(ef.path("ruleId").asText());
      assertThat(af.path("severity").asText()).isEqualTo(ef.path("severity").asText());
      assertThat(af.path("message").asText()).isEqualTo(ef.path("message").asText());
    }
  }

  @Test
  void caseNumberValid001_fromFixtureFile() throws Exception {
    String body =
        java.nio.file.Files.readString(
            Path.of("../fixtures/case-number/valid-001.request.json"), StandardCharsets.UTF_8);
    ResponseEntity<String> resp =
        rest.postForEntity("/api/v1/case-number/validate", jsonEntity(body), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode n = objectMapper.readTree(resp.getBody());
    assertThat(n.path("valid").asBoolean()).isTrue();
    assertThat(n.path("ruleRefs").get(0).path("ruleId").asText()).isEqualTo("CN-YEAR-SP-001");
  }

  @Test
  void caseNumberValid002_fromFixture_expectedGolden() throws Exception {
    String body =
        Files.readString(Path.of("../fixtures/case-number/valid-002.request.json"), StandardCharsets.UTF_8);
    ResponseEntity<String> resp =
        rest.postForEntity("/api/v1/case-number/validate", jsonEntity(body), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertCaseNumberBodyMatchesGolden(
        resp.getBody(), Path.of("../fixtures/case-number/valid-002.expected.json"));
  }

  @Test
  void caseNumberInvalid001_fromFixture_expectedGolden() throws Exception {
    String body =
        Files.readString(Path.of("../fixtures/case-number/invalid-001.request.json"), StandardCharsets.UTF_8);
    ResponseEntity<String> resp =
        rest.postForEntity("/api/v1/case-number/validate", jsonEntity(body), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertCaseNumberBodyMatchesGolden(
        resp.getBody(), Path.of("../fixtures/case-number/invalid-001.expected.json"));
  }

  @Test
  void caseNumberInvalid002_fromFixture_expectedGolden() throws Exception {
    String body =
        Files.readString(Path.of("../fixtures/case-number/invalid-002.request.json"), StandardCharsets.UTF_8);
    ResponseEntity<String> resp =
        rest.postForEntity("/api/v1/case-number/validate", jsonEntity(body), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertCaseNumberBodyMatchesGolden(
        resp.getBody(), Path.of("../fixtures/case-number/invalid-002.expected.json"));
  }

  @Test
  void documentExtract_successSample_fromFixtureFile() throws Exception {
    String body =
        java.nio.file.Files.readString(
            Path.of("../fixtures/document-extract/success-sample.request.json"),
            StandardCharsets.UTF_8);
    ResponseEntity<String> resp =
        rest.postForEntity("/api/v1/document/extract", jsonEntity(body), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode ex = objectMapper.readTree(resp.getBody()).path("extract");
    assertThat(ex.path("caseNumberRaw").asText()).isEqualTo("（2024）鄂0102民初10001号");
    assertThat(ex.path("courtNameSnippet").asText()).isEqualTo("湖北省武汉市江岸区人民法院");
    assertThat(ex.path("causeSnippet").asText()).isEqualTo("买卖合同纠纷");
    assertThat(ex.path("judgementDateISO").asText()).isEqualTo("2024-11-01");
  }

  @Test
  void documentExtract_pipelineMismatch_fromFixtureFile() throws Exception {
    String body =
        java.nio.file.Files.readString(
            Path.of("../fixtures/document-extract/pipeline-mismatch.request.json"),
            StandardCharsets.UTF_8);
    ResponseEntity<String> resp =
        rest.postForEntity("/api/v1/document/extract", jsonEntity(body), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    JsonNode n = objectMapper.readTree(resp.getBody());
    assertThat(n.path("errorCode").asText()).isEqualTo("PIPELINE_MISMATCH");
    assertThat(n.path("message").asText())
        .isEqualTo("输入正文缺少民事判决书管线所需的固定标记（演示：PIPELINE_MISMATCH）。");
  }

  @Test
  void compliance_deterministicHit_sampleFile() throws Exception {
    String source =
        java.nio.file.Files.readString(
            Path.of("../fixtures/compliance/deterministic-hit.sample.txt"), StandardCharsets.UTF_8);
    String payload = objectMapper.writeValueAsString(java.util.Map.of("source", source));
    ResponseEntity<String> resp =
        rest.postForEntity("/api/v1/compliance/scan", jsonEntity(payload), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode n = objectMapper.readTree(resp.getBody());
    assertThat(n.path("findings").get(0).path("ruleId").asText())
        .isEqualTo("R-DEMO-ID-LIKE-DIGITS");
    assertThat(n.path("findings").get(0).path("severity").asText()).isEqualTo("deterministic");
  }

  @Test
  void compliance_clean_fromFixture_requestAndExpectedGolden() throws Exception {
    String body =
        Files.readString(Path.of("../fixtures/compliance/clean.request.json"), StandardCharsets.UTF_8);
    ResponseEntity<String> resp =
        rest.postForEntity("/api/v1/compliance/scan", jsonEntity(body), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertComplianceFindingsMatchGolden(resp.getBody(), Path.of("../fixtures/compliance/clean.expected.json"));
  }

  @Test
  void compliance_suspiciousOnly_fromFixture_requestAndExpectedGolden() throws Exception {
    String body =
        Files.readString(Path.of("../fixtures/compliance/suspicious-only.request.json"), StandardCharsets.UTF_8);
    ResponseEntity<String> resp =
        rest.postForEntity("/api/v1/compliance/scan", jsonEntity(body), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertComplianceFindingsMatchGolden(
        resp.getBody(), Path.of("../fixtures/compliance/suspicious-only.expected.json"));

    JsonNode findings = objectMapper.readTree(resp.getBody()).path("findings");
    for (JsonNode f : findings) {
      assertThat(f.path("severity").asText()).isNotEqualTo("deterministic");
    }
  }
}
