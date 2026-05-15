# HTTP 状态与错误载荷对照（MVP）

机器可读定义见 [contracts/openapi.yaml](../../contracts/openapi.yaml) 组件：`ErrorResponse`、`ExtractErrorResponse`，以及 **`components.responses.GatewayTimeout`**（504）。

## 成功

| HTTP | 端点 | 响应 schema |
|------|------|-------------|
| 200 | `POST /api/v1/case-number/validate` | `CaseNumberValidateResponse` |
| 200 | `POST /api/v1/document/extract` | `DocumentExtractResponse` |
| 200 | `POST /api/v1/compliance/scan` | `ComplianceScanResponse` |

## 客户端 / 契约校验错误

| HTTP | 含义 | Body schema | 说明 |
|------|------|-------------|------|
| 400 | 畸形请求 | `ErrorResponse` | 缺 body、非法 JSON、不满足请求 schema（缺必填、类型错误、`additionalProperties: false`、枚举不符、`schemaVersion` 非冻结值等） |

`ErrorResponse` 必填字段：`error`（字符串）、`message`（字符串）。

**`error` 建议取值（叙事约定，OpenAPI 未收紧 enum）：**

| `error`（建议） | 典型场景 |
|-----------------|----------|
| `BAD_REQUEST` | 字段缺失、类型错误、违反 `additionalProperties: false`、枚举不符 |
| `INVALID_JSON` | 请求体非合法 JSON（若实现区分） |
| `REQUEST_TIMEOUT` | **仅**与 **HTTP 504** 配对：处理预算耗尽（见 [README.md](../../README.md)、OpenAPI `GatewayTimeout`） |

实现可选用其它稳定字符串，但应在 README 或本文档补充列表以便 **G** 断言；未经 MVP 变更流程不建议频繁改名。

## 文书抽取结构化失败（仅此端点）

| HTTP | 含义 | Body schema |
|------|------|-------------|
| 422 | 输入形状合法但**不匹配**冻结管线 / 标记不足 / 组装不一致等（**禁止**转远端 LLM） | `ExtractErrorResponse` |

**不得**用 **422** 表示**墙钟处理超时**；超时统一 **504** + `ErrorResponse`（见下节）。

`ExtractErrorResponse` 必填：`errorCode`、`message`。可选：`ruleRefs`。

**`errorCode` 枚举（契约冻结，仅用于 HTTP 422）：**

| `errorCode` | 说明 |
|-------------|------|
| `PIPELINE_MISMATCH` | 正文不属于 `civil-judgment-v1` 可解析范围（验收 **S2-OOR** 首要期望） |
| `INSUFFICIENT_MARKERS` | 缺少必要锚点/标记，无法稳定抽取 |
| `PARSE_TIMEOUT` | **保留枚举值**；**MVP 下不得**用于「单次请求处理预算耗尽」——该情况必须返回 **504** + `REQUEST_TIMEOUT`。保留仅为扩展或非墙钟语义预留（若实现永不返回本码，可与 **G** 约定断言范围）。 |
| `SCHEMA_INTERNAL` | 内部一致性/组装错误（演示环境少见，保留稳定码） |

## 处理超时（三条 MVP 路径一致）

| HTTP | 含义 | Body schema |
|------|------|-------------|
| 504 | 服务端处理预算耗尽（默认 ≤10s，见 [README.md](../../README.md)、[buchong.md](../buchong.md)） | `ErrorResponse`，且 **`error=REQUEST_TIMEOUT`** |

OpenAPI 通过 **`#/components/responses/GatewayTimeout`** 已为三路 POST **声明 504**；示例载荷见 [examples.md](./examples.md)。

## 案号校验注意

合法 / 非法均可返回 **HTTP 200**，由 `valid` 字段区分；无效时的字段不变式见 [invariants.md](./invariants.md)。
