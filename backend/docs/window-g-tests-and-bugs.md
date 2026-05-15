# 窗口 G：黄金用例集成测试与缺陷记录（backend）

真源：[docs/golden-fixtures.md](../../docs/golden-fixtures.md)、[contracts/openapi.yaml](../../contracts/openapi.yaml)、[README.md](../../README.md) 超时/504 声明。

## 覆盖矩阵对照表（golden-fixtures 行号 ↔ `@Test` 方法）

| golden-fixtures.md 行号 | scenarioId | 测试方法名 |
|-------------------------|------------|------------|
| 46 | S1-V-001 | `caseNumberValid001_fromFixtureFile` |
| 47 | S1-V-002 | `caseNumberValid002_fromFixture_expectedGolden` |
| 52 | S1-I-001 | `caseNumberInvalid001_fromFixture_expectedGolden` |
| 53 | S1-I-002 | `caseNumberInvalid002_fromFixture_expectedGolden` |
| 75 | S2-SUCCESS | `documentExtract_successSample_fromFixtureFile` |
| 77 | S2-OOR | `documentExtract_pipelineMismatch_fromFixtureFile` |
| 88 | S3-Scan-HitDeterministic | `compliance_deterministicHit_sampleFile` |
| 89 | S3-Scan-Suspicious | `compliance_suspiciousOnly_fromFixture_requestAndExpectedGolden` |
| 90 | S3-Scan-Clean | `compliance_clean_fromFixture_requestAndExpectedGolden` |

未在本 IT 挂载的行：S1-V-003…V-006、S1-I-003…I-006、S2 其余文书场景、S3-Scan-BothLevels、S4（自检声明）。后续可按同一模式从 `../fixtures/` 扩充。

## 测试用例摘要（fixture 路径）

| 方法 | 请求体来源 | 对照期望 |
|------|------------|----------|
| `caseNumberValid001_fromFixtureFile` | `fixtures/case-number/valid-001.request.json` | 字段抽样（与初版一致） |
| `caseNumberValid002_fromFixture_expectedGolden` | `valid-002.request.json` | `valid-002.expected.json`（去 `_meta` 后逐字段，含 `normalized` / `ruleRefs`；`message`/`reasonCode` 空与缺省属等价） |
| `caseNumberInvalid001_fromFixture_expectedGolden` | `invalid-001.request.json` | `invalid-001.expected.json`（含 `message` 全文） |
| `caseNumberInvalid002_fromFixture_expectedGolden` | `invalid-002.request.json` | `invalid-002.expected.json` |
| `documentExtract_successSample_fromFixtureFile` | `success-sample.request.json` | `extract` 关键字段 |
| `documentExtract_pipelineMismatch_fromFixtureFile` | `pipeline-mismatch.request.json` | HTTP 422 + `PIPELINE_MISMATCH` |
| `compliance_deterministicHit_sampleFile` | 由 `deterministic-hit.sample.txt` 组 JSON | `R-DEMO-ID-LIKE-DIGITS` / `deterministic` |
| `compliance_clean_fromFixture_requestAndExpectedGolden` | `clean.request.json`（含 `_meta`，后端忽略） | `clean.expected.json` → `findings: []` |
| `compliance_suspiciousOnly_fromFixture_requestAndExpectedGolden` | `suspicious-only.request.json` | `suspicious-only.expected.json`；并断言无任何 `deterministic` 级别 finding |

运行：`cd backend && mvn test`（Surefire 已包含 `**/*IT.java`）。

## 本轮测试暴露的缺陷与修复（非契约变更）

**现象（集成测试失败）**

- **契约/黄金**：案号非法响应 `message` 须与 `fixtures/case-number/invalid-001|002.expected.json` 一致（见 golden-fixtures S1 说明）。
- **合规**：`suspicious-only.expected.json` 中 `ComplianceFinding.message` 为非空中文文案。

**根因**

- 标准库 YAML 使用 `summary_zh`、`message_zh` 等键名，而 `yamlObjectMapper` 默认按 Java 属性名 `summaryZh`、`messageZh` 绑定，导致反序列化后字段为空；`CaseNumberService` 回落到默认 copy，`ComplianceScanService` 传出空 `message`。

**修复**

- 在 `com.legaldev.mvp.stdlib.StdlibBootstrap` 的记录型 DTO 上增加 Jackson 映射：`CaseNumberRuleRaw.summaryZh` → `@JsonProperty("summary_zh")`；`ComplianceRuleRaw.messageZh` → `@JsonAlias({"message_zh", "message"})`；`CleanScanFixtureRefs.descriptionZh` → `@JsonProperty("description_zh")`（完整性）。

**复现（修复前）**

1. `cd backend && mvn test`
2. 观察 `caseNumberInvalid001_fromFixture_expectedGolden`、`caseNumberInvalid002_fromFixture_expectedGolden`、`compliance_suspiciousOnly_fromFixture_requestAndExpectedGolden` 对 `message` 的断言失败。

## 粗检：日志 / 配置与完整粘贴正文

- **应用代码**：`backend/src/main` 下未发现 `Logger` / `System.out` / `printStackTrace` 对用户输入做全文记录。
- **配置**：[application.yml](../src/main/resources/application.yml) 无第三方 API Key；超时与 Tomcat 容量为体量声明。
- **建议**：后续若增加请求日志中间件，须避免记录 `DocumentExtractRequest.content`、`ComplianceScanRequest.source` 等全文字段；仅保留长度、哈希或合规脱敏片段。
