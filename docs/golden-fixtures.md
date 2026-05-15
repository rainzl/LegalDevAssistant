# 黄金用例与规则映射（参赛 MVP）

**契约：** [contracts/openapi.yaml](../contracts/openapi.yaml)（只读对齐；变更须经窗口 D）  
**对照验收：** [mvp-acceptance.md](./mvp-acceptance.md)  
**步骤 8（buchong）：** 本矩阵当前 **20** 条可脚本断言场景（`scenarioId`），覆盖案号分支、文书抽取正负例、合规分级与组合命中。

本文档给出 **可断言** 的矩阵：每条规则有稳定 **ID**，fixture 含 **请求体 / 正文** 与 **`*.expected.json`**。`_meta.scenarioId` / `_meta.primaryRuleIds` 供窗口 G 挂载测试名。

---

## 标准库与规则数据来源（总则）

| 资产 | sourcing | 维护说明 |
|------|----------|----------|
| 案号格式规则 `CN-*` | **synthetic-demo** | `fixtures/stdlib/rules-case-number-demo.yaml` |
| 合规规则 `R-*` | **synthetic-demo** | `fixtures/stdlib/compliance-rules.v0_1_0_demo.yaml` |
| 文书管线 `civil-judgment-v1` | **synthetic-demo** | `fixtures/stdlib/document-pipelines.v0_1_0_demo.yaml` |

---

## 规则变更 → 须同步的工件（协作清单）

| 规则 ID / 束 | 影响的 fixture / 文档（最小集） |
|----------|----------------------------------|
| `CN-YEAR-SP-001` | `fixtures/case-number/valid-*.{request,expected}.json`、`fixtures/stdlib/rules-case-number-demo.yaml`、本文件 **S1** |
| `CN-YEAR-SP-404` | `fixtures/case-number/invalid-001,003,004,005,006.*`（见 S1 脚注）、`rules-case-number-demo.yaml`、本文件 **S1** |
| `CN-FORMAT-JUNK` | `fixtures/case-number/invalid-002.*`、`rules-case-number-demo.yaml`、本文件 **S1** |
| `R-DEMO-ID-LIKE-DIGITS` | `fixtures/compliance/deterministic-hit.*`、`both-severities.*`、`compliance-rules.v0_1_0_demo.yaml`、本文件 **S3** |
| `R-DEMO-IDNUM-KEYWORD` | `fixtures/compliance/suspicious-only.*`、`both-severities.*`、`compliance-rules.v0_1_0_demo.yaml`、本文件 **S3** |
| 合规零命中束 | `fixtures/compliance/clean.*`、`compliance-rules` 中 `cleanScanFixtureRefs` |
| 合规双命中束 | `fixtures/compliance/both-severities.*`、`compliance-rules` 中 `dualHitFixtureRefs` |
| `civil-judgment-v1` | `fixtures/document-extract/**`、`document-pipelines.v0_1_0_demo.yaml`、本文件 **S2** |

---

## S1｜案号校验（`/api/v1/case-number/validate`）

### 脚注（与实现对齐）

- **`CN-YEAR-SP-404` 在演示后端中有两重触发**：① 缺少年份 token `（YYYY）`；② 年份 token 存在但 `synthetic_accept_pattern` 未命中（此时 `reasonCode` 仍为 `CN-YEAR-SP-404`，**message** 取自 `CN-YEAR-SP-001.summary_zh`）。详见 `fixtures/stdlib/rules-case-number-demo.yaml` 中 `CN-YEAR-SP-404.note_zh` 与 **`S1-I-003`** 期望文件。

### 测试矩阵（断言字段）

| scenarioId | 请求 fixture | 期望 fixture | `candidate`（摘录） | `valid` | `reasonCode`（非法） | `ruleRefs[].ruleId` |
|------------|----------------|----------------|---------------------|---------|----------------------|---------------------|
| S1-V-001 | `fixtures/case-number/valid-001.request.json` | `fixtures/case-number/valid-001.expected.json` | `（2024）鄂0102民初10001号` | `true` | `null` | `CN-YEAR-SP-001` |
| S1-V-002 | `fixtures/case-number/valid-002.request.json` | `fixtures/case-number/valid-002.expected.json` | `（2023）粤01民终888号` | `true` | `null` | `CN-YEAR-SP-001` |
| S1-V-003 | `fixtures/case-number/valid-003.request.json` | `fixtures/case-number/valid-003.expected.json` | `（2024）京0101刑初999号` | `true` | `null` | `CN-YEAR-SP-001` |
| S1-V-004 | `fixtures/case-number/valid-004.request.json` | `fixtures/case-number/valid-004.expected.json` | `（2022）沪0115行初42号` | `true` | `null` | `CN-YEAR-SP-001` |
| S1-V-005 | `fixtures/case-number/valid-005.request.json` | `fixtures/case-number/valid-005.expected.json` | `（2023）浙01民终12号` | `true` | `null` | `CN-YEAR-SP-001` |
| S1-V-006 | `fixtures/case-number/valid-006.request.json` | `fixtures/case-number/valid-006.expected.json` | `（2021）川0104刑终3号` | `true` | `null` | `CN-YEAR-SP-001` |
| S1-I-001 | `fixtures/case-number/invalid-001.request.json` | `fixtures/case-number/invalid-001.expected.json` | `鄂0102民初10001号`（缺 `（YYYY）`） | `false` | `CN-YEAR-SP-404` | `CN-YEAR-SP-404` |
| S1-I-002 | `fixtures/case-number/invalid-002.request.json` | `fixtures/case-number/invalid-002.expected.json` | `%%%NOT-A-CASE-ID%%%` | `false` | `CN-FORMAT-JUNK` | `CN-FORMAT-JUNK` |
| S1-I-003 | `fixtures/case-number/invalid-003.request.json` | `fixtures/case-number/invalid-003.expected.json` | `（2024）演示法院民事裁定书123号`（无 `民初|刑初|…`） | `false` | `CN-YEAR-SP-404` | `CN-YEAR-SP-404` |
| S1-I-004 | `fixtures/case-number/invalid-004.request.json` | `fixtures/case-number/invalid-004.expected.json` | `(2024)鄂0102民初10001号`（ASCII 括号） | `false` | `CN-YEAR-SP-404` | `CN-YEAR-SP-404` |
| S1-I-005 | `fixtures/case-number/invalid-005.request.json` | `fixtures/case-number/invalid-005.expected.json` | `   `（trim 空） | `false` | `CN-YEAR-SP-404` | `CN-YEAR-SP-404` |
| S1-I-006 | `fixtures/case-number/invalid-006.request.json` | `fixtures/case-number/invalid-006.expected.json` | `演示案号占位文本` | `false` | `CN-YEAR-SP-404` | `CN-YEAR-SP-404` |

**非法样例 `message`：** 须与对应 `*.expected.json` 全文一致（取自 stdlib `summary_zh`，**S1-I-003** 例外见脚注）。

### 规则真源（与 YAML 一致）

| ruleId | kind | 机器可读条件（摘录） |
|--------|------|----------------------|
| `CN-YEAR-SP-001` | `synthetic_accept_pattern` | `^（[0-9]{4}）.*(?:民初|刑初|行初|民终|刑终|行终).*?[0-9]+号$` |
| `CN-YEAR-SP-404` | `synthetic_reject_missing_token` | 子串须匹配 `（[0-9]{4}）`；其余回落语义见脚注 |
| `CN-FORMAT-JUNK` | `synthetic_junk_classifier` | `^[%._/A-Za-z0-9-]+$` |

---

## S2｜文书抽取单管线（`/api/v1/document/extract`）

| scenarioId | 请求 fixture | 正文真源（LF） | 成功期望 | HTTP 422 期望 |
|------------|----------------|----------------|-----------|----------------|
| S2-SUCCESS | `fixtures/document-extract/success-sample.request.json` | `success-sample.txt` | `success-sample.expected.json` → `extract` | — |
| S2-SUCCESS-NO-DATE | `fixtures/document-extract/success-no-date.request.json` | `success-no-date.txt` | `success-no-date.expected.json`（`judgementDateISO=null`） | — |
| S2-OOR | `fixtures/document-extract/pipeline-mismatch.request.json` | `pipeline-mismatch-sample.txt` | — | `pipeline-mismatch.expected.json`：`PIPELINE_MISMATCH` |
| S2-INSUFFICIENT-MARKERS | `fixtures/document-extract/insufficient-markers.request.json` | `insufficient-markers-sample.txt` | — | `insufficient-markers.expected.json`：`INSUFFICIENT_MARKERS` |

**常量：** `pipelineId=civil-judgment-v1`，`schemaVersion=2026-05-13`，`contentType=text/plain`，`encoding=UTF-8`

---

## S3｜合规扫描（`/api/v1/compliance/scan`）

| scenarioId | `source` fixture | POST 体贴片 | 期望 |
|------------|------------------|-------------|------|
| S3-Scan-HitDeterministic | `fixtures/compliance/deterministic-hit.sample.txt` | `deterministic-hit.request.json` | `deterministic-hit.expected.json` |
| S3-Scan-Suspicious | `fixtures/compliance/suspicious-only.sample.txt` | `suspicious-only.request.json` | `suspicious-only.expected.json`（不得出现 `deterministic`） |
| S3-Scan-Clean | `fixtures/compliance/clean.sample.txt` | `clean.request.json` | `clean.expected.json` → `findings: []` |
| S3-Scan-BothLevels | `fixtures/compliance/both-severities.sample.txt` | `both-severities.request.json` | `both-severities.expected.json`（两规则、顺序=YAML 声明序） |

### 规则真源（ID + 引擎 + 模式）

| ruleId | severity | engine | pattern / tokens |
|--------|----------|--------|------------------|
| `R-DEMO-ID-LIKE-DIGITS` | deterministic | regex | `\d{15,}` |
| `R-DEMO-IDNUM-KEYWORD` | suspicious | literal_any | `身份证号码`、`身份证号` |

---

## S4｜数据红线自检

`fixtures/**/*` 均为 synthetic demo；禁止真实可溯源案情字段。

---

## Fixture 索引（路径）

- **案号：** `fixtures/case-number/valid-{001..006}.{request,expected}.json`、`invalid-{001..006}.{request,expected}.json`
- **文书：** `fixtures/document-extract/success-sample.*`、`success-no-date.*`、`pipeline-mismatch*`、`insufficient-markers*`
- **合规：** `fixtures/compliance/deterministic-hit.*`、`suspicious-only.*`、`clean.*`、`both-severities.*`
- **标准库：** `fixtures/stdlib/*.yaml`

---

## VERSION

- **矩阵版本：** `2026-05-14`（≥20 `scenarioId`；增补文书 INSUFFICIENT / 无日期 / 合规双命中）
- **维护者（占位）：** `competition-maintainer`
