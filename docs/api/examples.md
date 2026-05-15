# 最小请求示例（与 `fixtures/` 对齐）

下列示例假定 API 基址为 `http://localhost:8080` ，请按实际部署替换。`Content-Type: application/json`。

## 1. 案号校验

与 `fixtures/case-number/*.request.json` 同形；**JSON 断言**见配对文件 `fixtures/case-number/*.expected.json`（与 [golden-fixtures.md](../golden-fixtures.md) S1 矩阵一致）。

**合法示例（valid-001）：**

```http
POST /api/v1/case-number/validate
Content-Type: application/json

{"candidate": "（2024）鄂0102民初10001号"}
```

**合法示例（valid-002）：**

```json
{"candidate": "（2023）粤01民终888号"}
```

**非法示例（invalid-001）：**

```json
{"candidate": "鄂0102民初10001号"}
```

**非法示例（invalid-002）：**

```json
{"candidate": "%%%NOT-A-CASE-ID%%%"}
```

## 2. 文书抽取（冻结常量）

**回归真源：** [`fixtures/document-extract/success-sample.request.json`](../../fixtures/document-extract/success-sample.request.json)（`content` 与 [`success-sample.txt`](../../fixtures/document-extract/success-sample.txt) 字节一致）。期望 `extract` 见 [`success-sample.expected.json`](../../fixtures/document-extract/success-sample.expected.json) 的 **`extract`** 键（忽略 `_meta`）。

```http
POST /api/v1/document/extract
Content-Type: application/json
```

下方 JSON 仅作速览；若与 `success-sample.request.json` 不一致，以 **`success-sample.request.json` 为准**。

```json
{
  "pipelineId": "civil-judgment-v1",
  "schemaVersion": "2026-05-13",
  "content": "湖北省武汉市江岸区人民法院\n民事判决书\n\n（2024）鄂0102民初10001号\n\n本案系演示用买卖合同纠纷案件。\n案由：买卖合同纠纷。\n文书日期：2024-11-01\n",
  "contentType": "text/plain",
  "encoding": "UTF-8"
}
```

`encoding` 可省略，契约默认 `UTF-8`。

**可选日期：** [`success-no-date.request.json`](../../fixtures/document-extract/success-no-date.request.json) / [`success-no-date.expected.json`](../../fixtures/document-extract/success-no-date.expected.json)（`scenarioId=S2-SUCCESS-NO-DATE`）。

**标记不足（契约已有 errorCode）：** [`insufficient-markers.request.json`](../../fixtures/document-extract/insufficient-markers.request.json) → [`insufficient-markers.expected.json`](../../fixtures/document-extract/insufficient-markers.expected.json)（`INSUFFICIENT_MARKERS`，`scenarioId=S2-INSUFFICIENT-MARKERS`）。

**管线不匹配负例**正文见 [`fixtures/document-extract/pipeline-mismatch-sample.txt`](../../fixtures/document-extract/pipeline-mismatch-sample.txt)；期望 **HTTP 422**，`errorCode` **`PIPELINE_MISMATCH`**，正文与 `fixtures/document-extract/pipeline-mismatch.expected.json` 对齐（见 [errors.md](./errors.md)）。

## 3. 合规扫描

将下列文件全文作为 `source` 字符串（换行保留）。

| 场景 | `source` 正文 | POST 体贴片 | `findings` 期望 |
|------|----------------|-------------|-------------------|
| deterministic 命中 | [`deterministic-hit.sample.txt`](../../fixtures/compliance/deterministic-hit.sample.txt) | [`deterministic-hit.request.json`](../../fixtures/compliance/deterministic-hit.request.json) | [`deterministic-hit.expected.json`](../../fixtures/compliance/deterministic-hit.expected.json) |
| 仅 suspicious | [`suspicious-only.sample.txt`](../../fixtures/compliance/suspicious-only.sample.txt) | [`suspicious-only.request.json`](../../fixtures/compliance/suspicious-only.request.json) | [`suspicious-only.expected.json`](../../fixtures/compliance/suspicious-only.expected.json) |
| deterministic + suspicious 同现 | [`both-severities.sample.txt`](../../fixtures/compliance/both-severities.sample.txt) | [`both-severities.request.json`](../../fixtures/compliance/both-severities.request.json) | [`both-severities.expected.json`](../../fixtures/compliance/both-severities.expected.json) |

示例（deterministic）：

```http
POST /api/v1/compliance/scan
Content-Type: application/json
```

```json
{
  "source": "// DEMO ONLY — synthetic digit run resembling ID layout; NOT a real person's number.\nconst legacyDemoToken = '9876543210123456789';\nlogDebug(legacyDemoToken);\n"
}
```

规则 ID 与 severity 期望见 [docs/golden-fixtures.md](../golden-fixtures.md) **S3**。

## 4. 错误响应示例（契约形状）

### HTTP 504 — 处理超时（三路 POST 一致）

与 OpenAPI `components.responses.GatewayTimeout`、[README.md](../../README.md) 冻结一致：

```json
{
  "error": "REQUEST_TIMEOUT",
  "message": "处理超时，请缩短输入或稍后重试。"
}
```

### HTTP 422 — 文书抽取结构化失败（仅此端点）

与黄金 **`pipeline-mismatch.expected.json`** 冻结中文 `message` 一致（回归脚本请断言该字符串）：

```json
{
  "errorCode": "PIPELINE_MISMATCH",
  "message": "输入正文缺少民事判决书管线所需的固定标记（演示：PIPELINE_MISMATCH）。"
}
```

### HTTP 400 — 畸形请求

```json
{
  "error": "BAD_REQUEST",
  "message": "必填字段缺失或类型不正确。"
}
```
