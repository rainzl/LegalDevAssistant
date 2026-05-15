# 契约不易表达的不变式（G / F 必读）

本文档补充 [contracts/openapi.yaml](../../contracts/openapi.yaml) ；冲突时以 OpenAPI 字段为准，本文仅描述**条件逻辑**与 **fixture 约定**。

## 案号：`POST /api/v1/case-number/validate`

OpenAPI 对 `CaseNumberValidateResponse` 仅 `required: [valid]`。验收 **S1-CaseId-Invalid** 要求无效时原因可读且不矛盾，固化为：

| 条件 | 约束 |
|------|------|
| `valid=true` | `normalized` **宜**给出规范化案号字符串；`reasonCode` 可为 `null` |
| `valid=false` | `reasonCode` 与 `message` **必须**均为**非空**字符串，且语义一致（不把合法说成非法或反之） |

`ruleRefs` 可出现；黄金用例期望的 ruleId 见 [docs/golden-fixtures.md](../golden-fixtures.md)。

## 文书：`POST /api/v1/document/extract` 请求

- **`pipelineId`** 必须为 **`civil-judgment-v1`**（OpenAPI 枚举）。
- **`schemaVersion`** 必须为 **`2026-05-13`**（与 [docs/mvp-acceptance.md](../mvp-acceptance.md) 及黄金用例冻结一致；OpenAPI 已为请求与 `CivilJudgmentV1Extract` 收紧 **enum**）。其它字符串不按 MVP 受理。
- **`contentType`**：`text/plain`；**`encoding`**：省略则等价 **`UTF-8`**。

## 文书：`DocumentExtractResponse`

- API 响应**仅**包含 OpenAPI 定义的 `extract` 对象（即 `CivilJudgmentV1Extract`），**不得**依赖 fixture 中的额外键。
- [`fixtures/document-extract/success-sample.expected.json`](../../fixtures/document-extract/success-sample.expected.json) 顶层的 **`_meta` 仅用于样例说明与人工注释**，**不是** API 响应字段；自动化断言应在校验 schema 后比对 **`extract` 子对象**与期望字段。

## 合规：`ComplianceScanResponse`

- `findings` 恒为数组（可为空）。**S3-Scan-Clean** 期望无 deterministic 命中；是否允许仅 `suggestion` 级命中以 [docs/mvp-acceptance.md](../mvp-acceptance.md) 为准（当前矩阵以「空或无 deterministic」为干净）。

## 请求体与 fixture 格式

- 案号黄金请求为 JSON：`{"candidate": "..."}` ，与 OpenAPI 一致。
- 合规黄金样例仓库中为 **`.txt` 源码片段**；调用 API 时需包装为 `{"source": "<文件全文>"}` ，可选 `label`。
