package com.legaldev.mvp.stdlib;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legaldev.mvp.config.StdlibProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Loads {@code fixtures/stdlib/stdlib-index.v0_1_0_demo.yaml} and referenced bundle YAML/JSON into
 * typed registries (single source of truth: repository {@code fixtures/stdlib}).
 */
@Component
public class StdlibBootstrap {

  private final ObjectMapper yamlMapper;
  private final ResourceLoader resourceLoader;
  private final StdlibProperties stdlibProperties;
  private final CaseNumberRulebook caseNumberRulebook = new CaseNumberRulebook();
  private final ComplianceRulebook complianceRulebook = new ComplianceRulebook();
  private final DocumentPipelineRegistry pipelineRegistry = new DocumentPipelineRegistry();

  public StdlibBootstrap(
      @Qualifier("yamlObjectMapper") ObjectMapper yamlMapper,
      ResourceLoader resourceLoader,
      StdlibProperties stdlibProperties) {
    this.yamlMapper = yamlMapper;
    this.resourceLoader = resourceLoader;
    this.stdlibProperties = stdlibProperties;
  }

  @PostConstruct
  public void load() throws IOException {
    Resource indexResource = resourceLoader.getResource(stdlibProperties.indexClasspath());
    StdlibIndex index = readYaml(indexResource.getInputStream(), StdlibIndex.class);
    for (StdlibIndex.LibraryRef ref : index.libraries()) {
      Resource bundleResource = resolveBundle(ref.bundle());
      switch (ref.libraryId()) {
        case "stdlib-case-number" -> loadCaseNumber(bundleResource);
        case "stdlib-compliance" -> loadCompliance(bundleResource);
        case "stdlib-document-pipelines" -> loadPipelines(bundleResource);
        default ->
            throw new IllegalStateException(
                "Unknown libraryId in stdlib index: " + ref.libraryId());
      }
    }
  }

  private void loadCaseNumber(Resource bundleResource) throws IOException {
    CaseNumberStdlibFile file = readYaml(bundleResource.getInputStream(), CaseNumberStdlibFile.class);
    caseNumberRulebook.bind(file);
  }

  private void loadCompliance(Resource bundleResource) throws IOException {
    ComplianceStdlibFile file = readYaml(bundleResource.getInputStream(), ComplianceStdlibFile.class);
    complianceRulebook.bind(file);
  }

  private void loadPipelines(Resource bundleResource) throws IOException {
    DocumentPipelinesFile file = readYaml(bundleResource.getInputStream(), DocumentPipelinesFile.class);
    pipelineRegistry.bind(file);
  }

  private Resource resolveBundle(String bundlePathFromIndex) {
    String name =
        bundlePathFromIndex.contains("/")
            ? bundlePathFromIndex.substring(bundlePathFromIndex.lastIndexOf('/') + 1)
            : bundlePathFromIndex;
    String classpath = "classpath:fixtures/stdlib/" + name;
    return resourceLoader.getResource(classpath);
  }

  private <T> T readYaml(InputStream in, Class<T> type) throws IOException {
    return yamlMapper.readValue(in, type);
  }

  public CaseNumberRulebook caseNumbers() {
    return caseNumberRulebook;
  }

  public ComplianceRulebook compliance() {
    return complianceRulebook;
  }

  public DocumentPipelineRegistry pipelines() {
    return pipelineRegistry;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record StdlibIndex(
      String version, String maintainer, String source, List<LibraryRef> libraries) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LibraryRef(String libraryId, String bundle) {}
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record CaseNumberStdlibFile(
      String libraryId,
      String version,
      String maintainer,
      String source,
      List<CaseNumberRuleRaw> rules) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record CaseNumberRuleRaw(
      String id,
      String kind,
      @JsonProperty("summary_zh") String summaryZh,
      String demoPattern,
      String requiredTokenPattern,
      List<String> fixtureRefs) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ComplianceStdlibFile(
      String libraryId,
      String version,
      String maintainer,
      String source,
      CleanScanFixtureRefs cleanScanFixtureRefs,
      List<ComplianceRuleRaw> rules) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CleanScanFixtureRefs(
        @JsonProperty("description_zh") String descriptionZh, List<String> refs) {}
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ComplianceRuleRaw(
      String ruleId,
      String severity,
      @JsonAlias({"message_zh", "message"}) String messageZh,
      String engine,
      String pattern,
      List<String> tokens,
      List<String> fixtureRefs) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record DocumentPipelinesFile(
      String libraryId,
      String version,
      String maintainer,
      String source,
      List<PipelineRaw> pipelines) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record PipelineRaw(
      String pipelineId,
      String schemaVersion,
      List<String> contentTypes,
      List<String> encodings,
      String outputEnvelope,
      List<String> requiredFields,
      List<String> optionalFields,
      String extractHintsZh,
      List<String> fixtureRefs,
      List<String> mismatchFixtureRefs) {}
}
