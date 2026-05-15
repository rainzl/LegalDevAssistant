package com.legaldev.mvp.stdlib;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Document pipeline metadata from {@code document-pipelines.v0_1_0_demo.yaml}. */
public final class DocumentPipelineRegistry {

  private String libraryId;
  private String version;
  private String source;
  private final List<StdlibBootstrap.PipelineRaw> pipelines = new ArrayList<>();

  public void bind(StdlibBootstrap.DocumentPipelinesFile file) {
    this.libraryId = file.libraryId();
    this.version = file.version();
    this.source = file.source();
    this.pipelines.clear();
    this.pipelines.addAll(file.pipelines());
  }

  public Optional<StdlibBootstrap.PipelineRaw> find(String pipelineId, String schemaVersion) {
    return pipelines.stream()
        .filter(p -> pipelineId.equals(p.pipelineId()) && schemaVersion.equals(p.schemaVersion()))
        .findFirst();
  }

  public String libraryVersionRef() {
    return libraryId + "@v" + version;
  }
}
