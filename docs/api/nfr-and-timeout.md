# 非功能：10s 超时、降级与离线语义

与 [docs/buchong.md](../buchong.md)、[docs/mvp-acceptance.md](../mvp-acceptance.md)（**S0-NFR-Timeout**、**S2-Pipeline-Timeout**）、[docs/_架构说明_.md](../_架构说明_.md)、[README.md](../../README.md) 一致。

## 服务端处理时限

- 单次核心业务处理应在 **≤10 秒**内结束：要么返回完整成功响应，要么返回**确定的**超时或错误响应。
- 请求不得无限挂起；客户端建议配置 **约 10s** 的请求超时，并向用户展示可读提示。

## 超时对应的 HTTP 与响应体（仓库已冻结）

**真源：** [README.md](../../README.md)「超时（HTTP）映射声明」；**OpenAPI** 已为三条 MVP `POST` 声明 **`504`**，响应体 **`ErrorResponse`**（`#/components/responses/GatewayTimeout`），且 **`error=REQUEST_TIMEOUT`**。

| 项目 | MVP 冻结 |
|------|-----------|
| HTTP | **504 Gateway Timeout**（案号 / 文书抽取 / 合规 **一致**） |
| Body | **`ErrorResponse`**：`error=REQUEST_TIMEOUT`，`message` 可读（中文可） |

**文书抽取：** 管线语义失败仍用 **422** + **`ExtractErrorResponse`**（如 **`PIPELINE_MISMATCH`**）。**墙钟超时不得走 422**，也不得使用 **`ExtractErrorResponse`** 表达超时。

测试窗口 **G** 以 README + OpenAPI + 黄金用例为准。

## 离线失败语义（无外联密钥型 AI）

- MVP 核心业务路径**不得依赖**公网大模型或需 API Key 的云端推理；标准库与规则来自**仓库内**本地加载。
- **不应**将失败表现为依赖外部「上游 AI」的 **502 / 503** 语义（除非文档明确标注为容器/网关级错误且**不属于** MVP 契约承诺范围）。
- 与契约相关的失败类别应为：
  - **HTTP 400** + `ErrorResponse`（畸形 JSON、缺必填字段等）；
  - **HTTP 422** + `ExtractErrorResponse`（**仅**文书抽取结构化失败）；
  - **HTTP 504** + `ErrorResponse`（**REQUEST_TIMEOUT**，处理预算耗尽）。

## 并发

面向评委口径：**约 20 并发**设计预期；与超时策略一起在架构/README 中可追溯即可，不由 OpenAPI 字段表达。
