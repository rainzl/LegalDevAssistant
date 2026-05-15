# MVP HTTP API — 叙事契约与索引

本目录补充 [contracts/openapi.yaml](../../contracts/openapi.yaml) 的**文字约定**（不变式、错误语义、示例与非功能）。机器可读 schema 以 OpenAPI 为准。

## 真源顺序（实现 F / 测试 G）

1. **[contracts/openapi.yaml](../../contracts/openapi.yaml)** — 路径、组件 schema、`required`、枚举（如 `ExtractErrorResponse.errorCode`）。
2. **本目录** — OpenAPI 不易表达的**条件不变式**（例如案号 `valid=false` 时的字段）、超时 **504 + REQUEST_TIMEOUT**（README 冻结，OpenAPI `GatewayTimeout`）的叙事、离线失败语义。
3. **[docs/golden-fixtures.md](../golden-fixtures.md)** 与 **`fixtures/**`** — 黄金样例路径、期望 ruleId、Scenario S1–S4。

变更接口语义前须对齐 [docs/mvp-acceptance.md](../mvp-acceptance.md)，并同步黄金用例；禁止静默改语义。

## 文档索引

| 文件 | 内容 |
|------|------|
| [nfr-and-timeout.md](./nfr-and-timeout.md) | 10s、504/`REQUEST_TIMEOUT` 与 422 边界、离线本地失败语义 |
| [errors.md](./errors.md) | HTTP 状态与 `ErrorResponse` / `ExtractErrorResponse` 对照 |
| [invariants.md](./invariants.md) | 案号响应条件必填、fixture `_meta` 非 API 字段等 |
| [examples.md](./examples.md) | 与 `fixtures/` 对齐的最小请求示例 |

## MVP 三件套路径（摘要）

| 能力 | `POST` 路径 |
|------|-------------|
| 案号校验 | `/api/v1/case-number/validate` |
| 文书抽取（单管线） | `/api/v1/document/extract` |
| 合规扫描 | `/api/v1/compliance/scan` |

详见 OpenAPI `paths`。三条路由均声明 **HTTP 504**（`GatewayTimeout` → `ErrorResponse`）与 README 超时策略一致。
