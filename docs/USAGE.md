# Legal Dev Assistant MVP — 推广版使用说明

本文档面向**试用、路演与内训**：如何从零启动应用，并通过 **Web** 或 **HTTP API** 走完案号校验、文书抽取、合规扫描三条完整路径。技术细节以 [contracts/openapi.yaml](../contracts/openapi.yaml)、[docs/golden-fixtures.md](./golden-fixtures.md) 为准。

**数据红线：** 下文示例均来自仓库 **合成 fixture**；请勿在公开演示中替换为真实当事人或可识别案情。

---

## 1. 环境与启动

1. **后端**（默认 `http://localhost:8080`）

   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **前端**（默认 `http://localhost:5173`，`/api` 代理到 8080）

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. 浏览器打开 **`http://localhost:5173`**（开发）。若已通过 `backend/` 下 **`mvn clean package`** 打出可执行 JAR 并 **`java -jar …/legal-dev-mvp-0.1.0-mvp.jar`** 启动，则页面与 API 同源，请改用 **`http://localhost:8080`**（或与 `server.port` 一致），界面仍为 **三个 Tab**：**案号校验**、**文书抽取**、**合规扫描**（与 MVP 三件套一一对应）。

Windows 下调试命令行请求建议用 **`curl.exe`**，避免与 PowerShell 别名冲突（见 [README.md](../README.md)）。

---

## 2. 完整案例（Web 操作流程）

下列步骤按「先易后难」排列；每一步在对应 Tab 内点击页面上的 **校验 / 抽取 / 扫描** 按钮即可。

### 案例 A — 案号：合法（S1-V-001）

1. 打开 Tab **「案号校验」**。  
2. 在输入框粘贴：

   ```text
   （2024）鄂0102民初10001号
   ```

3. **期望（页面）：** 醒目 **「校验结果：合格」**（绿色）；**「归一化」** 与 **「规则引用」** 中含 **`CN-YEAR-SP-001`**（与 [`valid-001.expected.json`](../fixtures/case-number/valid-001.expected.json) 一致）。

---

### 案例 B — 案号：非法缺年号（S1-I-001）

1. 仍在 **案号校验**。  
2. 粘贴：

   ```text
   鄂0102民初10001号
   ```

3. **期望（页面）：** **「校验结果：不合格」**（红色）；**「原因码」** 为 **`CN-YEAR-SP-404`**；**「说明」** 可读；**「规则引用」** 列表含对应 **ruleId**（见 `invalid-001.expected.json`）。

---

### 案例 C — 文书抽取：成功（S2-SUCCESS）

1. 打开 Tab **「文书抽取」**。  
2. 将 [`fixtures/document-extract/success-sample.txt`](../fixtures/document-extract/success-sample.txt) **全文**复制进正文框（须含「民事判决书」、案号行、「案由：」、日期行等标记）。  
3. **期望（页面）：** **「文书抽取结果：成功」**（绿色）；下方结构化字段与 [`success-sample.expected.json`](../fixtures/document-extract/success-sample.expected.json) 中 **`extract`** 一致：`courtNameSnippet` →「湖北省武汉市江岸区人民法院」，`caseNumberRaw` →「（2024）鄂0102民初10001号」，`causeSnippet` →「买卖合同纠纷」，`judgementDateISO` → `2024-11-01`。

---

### 案例 D — 文书抽取：管线不匹配（S2-OOR）

1. 仍在 **文书抽取**。  
2. 将 [`fixtures/document-extract/pipeline-mismatch-sample.txt`](../fixtures/document-extract/pipeline-mismatch-sample.txt) 全文粘贴（刑事/决定书风格，不触发民事判决书管线）。  
3. **期望（页面）：** **「文书抽取结果：失败（HTTP 422）」**（红色）；**`errorCode`** 展示为 **`PIPELINE_MISMATCH`**；**`message`** 与契约/黄金样例一致；**不应**出现成功态的法院/案号字段块。

---

### 案例 E — 合规：确定级命中（S3-Scan-HitDeterministic）

1. 打开 Tab **「合规扫描」**。  
2. 将 [`fixtures/compliance/deterministic-hit.sample.txt`](../fixtures/compliance/deterministic-hit.sample.txt) 全文粘贴。  
3. **期望（页面）：** 摘要行 **「合规扫描结果：存在确定级命中（演示）。」**（琥珀色提示）；下方列表中至少一条 **`severity`** 为 **`deterministic`**，**`ruleId`** 为 **`R-DEMO-ID-LIKE-DIGITS`**，`message` 为演示语义（见 `deterministic-hit.expected.json`）。

---

### 案例 F — 合规：零命中（S3-Scan-Clean）

1. 仍在 **合规扫描**。  
2. 将 [`fixtures/compliance/clean.sample.txt`](../fixtures/compliance/clean.sample.txt) 全文粘贴。  
3. **期望（页面）：** 摘要行 **「合规扫描结果：零命中（findings 为空，演示）。」**（绿色）；正文提示「当前规则集未命中任何条目」，**不**展示命中列表。

---

### 案例 G — 合规：仅可疑级（S3-Scan-Suspicious）

1. 将 [`fixtures/compliance/suspicious-only.sample.txt`](../fixtures/compliance/suspicious-only.sample.txt) 粘贴到 **合规扫描**。  
2. **期望（页面）：** 摘要行 **「合规扫描结果：存在可疑级命中；无确定级命中（演示）。」**（琥珀色）；列表中可见 **`R-DEMO-IDNUM-KEYWORD`** / **`suspicious`**，**不应**出现 **`deterministic`**（见 `suspicious-only.expected.json`）。

---

## 3. 完整案例（命令行 / 集成调试）

基址 **`http://localhost:8080`**；`Content-Type: application/json`。

### 3.1 案号校验

```bash
curl.exe -s -X POST "http://localhost:8080/api/v1/case-number/validate" ^
  -H "Content-Type: application/json" ^
  -d "{\"candidate\":\"（2024）鄂0102民初10001号\"}"
```

**典型成功响应字段：** `"valid":true`，`"ruleRefs":[{"ruleId":"CN-YEAR-SP-001",...}]`。

### 3.2 文书抽取（从 fixture 文件读 body）

PowerShell 示例（路径按仓库根调整）：

```powershell
$body = Get-Content -Raw "fixtures\document-extract\success-sample.request.json" | ConvertFrom-Json | Select-Object pipelineId, schemaVersion, content, contentType, encoding | ConvertTo-Json -Compress
curl.exe -s -X POST "http://localhost:8080/api/v1/document/extract" -H "Content-Type: application/json" -d $body
```

**期望：** HTTP 200，JSON 内含 **`extract`** 对象，字段与 `success-sample.expected.json` 中 `extract` 一致。

**422 负例：** 使用 `pipeline-mismatch.request.json` 或手写与 `pipeline-mismatch-sample.txt` 一致的 `content`，期望 **`errorCode":"PIPELINE_MISMATCH"`**。

### 3.3 合规扫描

```bash
curl.exe -s -X POST "http://localhost:8080/api/v1/compliance/scan" ^
  -H "Content-Type: application/json" ^
  -d "{\"source\":\"// DEMO ONLY\\nconst legacyDemoToken = '9876543210123456789';\"}"
```

**期望：** `findings` 含 `R-DEMO-ID-LIKE-DIGITS`（细节以 [`deterministic-hit.request.json`](../fixtures/compliance/deterministic-hit.request.json) 全文对齐为准）。

---

## 4. 常见结果说明

| 现象 | 含义 |
|------|------|
| 案号结果始终「不合格」、但开发者工具里 JSON 已有 `"valid":true` | 响应体曾被当成 **字符串** 或未解析，`valid` 在内存中为 `undefined`；当前前端已对 **`application/json` 字符串体二次 `JSON.parse`** 并对 **`valid` 做布尔归一化**。仍异常时请确认后端 **`JacksonConfig`** 中 **`@Primary` `ObjectMapper` 为 JSON**（非仅 YAML Mapper）。 |
| **504** + `REQUEST_TIMEOUT` | 单次处理超过服务端预算（默认 10s），请缩短输入或稍后重试（三路径一致）。 |
| **422**（仅文书抽取） | 正文不符合 **`civil-judgment-v1`** 管线所需标记 → `ExtractErrorResponse`，**非**超时。 |
| **`findings: []`** | 当前规则集无命中（清洁样例）；非「接口失败」。 |

---

## 5. 延伸阅读

- 请求/响应形状与更多 curl：**[docs/api/examples.md](./api/examples.md)**  
- 场景 ID 与断言矩阵：**[docs/golden-fixtures.md](./golden-fixtures.md)**  
- 产品验收与不做清单：**[docs/mvp-acceptance.md](./mvp-acceptance.md)**  
- 答辩一页纸（打印/路演口播）：**[docs/defense-one-pager.md](./defense-one-pager.md)**
