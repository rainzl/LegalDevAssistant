# Legal Dev Assistant — Competition MVP（范围冻结）

本项目面向竞赛提交的最小 MVP：**案号校验**、**单条文书抽取管线**（`civil-judgment-v1`）、**本地合规扫描**。技术栈与设计约束见 [docs/buchong.md](docs/buchong.md)；分层、模块边界、非功能与部署见 [docs/_架构说明_.md](docs/_架构说明_.md)。

## 产品与验收（必读）

- **阶段 0 跨窗口冻结（真源）** — [docs/mvp-acceptance.md](docs/mvp-acceptance.md) 内 **「阶段 0 冻结（跨窗口真源）」**：三件套用户可见/后端必达、最小 GWT、答辩金句、风险与给架构窗口 B 的交接摘要  
- [docs/_架构说明_.md](docs/_架构说明_.md) — 架构与技术陈述（分层、模块、排除项核对；**第 5 节**为阶段 0 技术轮廓，与 `backend/` / `fixtures/` 现状对齐，供 D/C/F 执行）
- [docs/mvp-acceptance.md](docs/mvp-acceptance.md) — 用户故事、Given/When/Then、演示提纲、不做清单（全文）  
- [docs/golden-fixtures.md](docs/golden-fixtures.md) — Scenario S1–S4 对应的黄金样例与规则 ID  
- [contracts/openapi.yaml](contracts/openapi.yaml) — API 契约与文书抽取冻结 schema  
- [docs/api/README.md](docs/api/README.md) — 接口叙事契约（不变式、错误语义、示例与非功能补充）
- [docs/defense-one-pager.md](docs/defense-one-pager.md) — **答辩一页纸**（打印/口播：边界、数据流、演示三步、免责与效果口径）
- [docs/USAGE.md](docs/USAGE.md) — **推广版使用说明**（启动方式 + 案号/文书/合规 **7 条 Web 案例** + **curl** 示例）

## 超时（HTTP）映射声明（实现必填）

后端实现（`backend/`）已按下列策略固化；自动化测试以本文档声明 + [contracts/openapi.yaml](contracts/openapi.yaml) + [docs/api/nfr-and-timeout.md](docs/api/nfr-and-timeout.md) 为准。

| 项目 | 声明 |
|------|------|
| 服务端单次处理上限 | ≤10s；配置项 `app.processing.timeout-seconds`（默认 `10`），与 [docs/buchong.md](docs/buchong.md)、验收 **S0-NFR-Timeout** 一致 |
| 超时 HTTP 状态码 | **504 Gateway Timeout**（三条 MVP 路径一致） |
| 超时响应体 schema | **ErrorResponse**（`error=REQUEST_TIMEOUT`，`message` 为可读中文说明） |
| 三条 MVP 路径策略 | **全局一致**：案号校验、文书抽取、合规扫描均在同一处理线程预算内超时 → **504** + **ErrorResponse** |

文书抽取的结构化失败（管线不匹配等）仍按 OpenAPI 使用 **422** + **ExtractErrorResponse**（与黄金负例一致），**不**用作一般处理超时响应。

并发设计口径：Tomcat 线程与 `processingExecutor` 线程池在 `application.yml` / `ProcessingExecutorConfig` 中注释为面向 **~20 并发** 的容量提示（见 [docs/_架构说明_.md](docs/_架构说明_.md)）。

前端（`frontend/`）对 `/api` 的请求超时设为 **10s**（`src/api/client.js`），与上述预算对齐。

## 安全与隐私声明（对齐验收 S0-*）

本产品形态在参赛版本中**刻意不包含**以下内容，亦**不得在对外材料中虚假宣传**为其能力：

| 不包含 | 说明 |
|--------|------|
| 登录与账号体系 | 匿名试用；不设 RBAC/OAuth |
| 运行时 API Key / 云端大模型 | **不调用**依赖密钥的云端 LLM/SDK 作为核心业务路径 |
| 审计日志产品化 | **不设计**谁在何时处理了哪段业务的审计追踪 |

**数据处理边界（公网与外发）：** 核心业务（案号校验、文书抽取、合规扫描）**不得要求**用户将粘贴的文书或源码**上传到公网服务**即可完成。开发与部署应采用本机或内网环境；若在 CI 中存在外网拉取依赖，须与运行时业务链路区分并在答辩中如实说明。

**超时与非功能：** 面向评委的口径参见 [docs/buchong.md](docs/buchong.md)：**约 20 并发**设计预期、单次处理 **≤10s**，逾时应反馈**确定性**错误语义。具体 HTTP 与响应体组合见上文 **[超时（HTTP）映射声明](#超时http映射声明实现必填)** 与 [docs/api/nfr-and-timeout.md](docs/api/nfr-and-timeout.md)。

## _fixture 与红线

`fixtures/` 内样例全部为 **合成/演示**用途，不包含真实可对号当事人或可识别个人隐私数据。若在演示中使用自行粘贴文本，风险提示见 [mvp-acceptance Epic_E5](docs/mvp-acceptance.md)。

## 本地运行（开发）

**前置环境（阶段 2 脚手架）**：与 `backend/pom.xml`、`frontend/package.json` 对齐。

- **JDK**：**17+**（POM 属性 `java.version` 为 `17`；同一套代码可用 **21** 等更高 LTS 编译运行）。
- **构建后端**：**Maven**（建议 **3.8+**）。
- **构建前端**：**Node.js** + **npm**（`package.json` 未声明 `engines`；建议使用 **当前 Node LTS**。本仓库验收机示例：**JDK 21**、**Maven 3.9.x**、**Node 22 / npm 11**。）

**首次克隆后**：先在 `frontend/` 执行 `npm install`，再 `npm run dev`。

1. **后端（Spring Boot 3 / Java 17+）**

   ```bash
   cd backend
   mvn spring-boot:run
   ```

   默认监听 `http://localhost:8080`；标准库自仓库根目录 `fixtures/stdlib` 打包进类路径（见 `backend/pom.xml` 资源映射）。

2. **前端（Vue 3 + Vite + Element Plus）**

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

   开发服务器默认 `http://localhost:5173`，通过 Vite 将 **`/api` → `http://localhost:8080`**（与契约路径前缀 `/api/v1/...` 一致；见 `frontend/vite.config.js`）。前端请求体使用 **`/api` + `/v1/...`**，与 [contracts/openapi.yaml](contracts/openapi.yaml) 中 `paths` 一致。

3. **单进程运行（前端打进后端 JAR）**

   在 `backend/` 执行 **`mvn clean package -DskipTests`** 时，`prepare-package` 阶段会在 `../frontend` 下执行 **`npm ci`** 与 **`npm run build`**，并把 **`frontend/dist`** 复制到 **`target/classes/static`**，最终打进可执行 JAR。启动后**只需一个进程**即可同时提供页面与 API：

   ```bash
   cd backend
   mvn clean package -DskipTests
   java -jar target/legal-dev-mvp-0.1.0-mvp.jar
   ```

   浏览器访问 **`http://localhost:8080`**（与 `server.port` 一致）；接口仍为 **`/api/v1/...`**。`mvn test` **不会**触发前端构建，以保持单元测试轻快。

   - **仅打后端、跳过前端**：`-Dfrontend.skip=true`（JAR 内不含静态页，适用于纯 API 场景）。
   - **Node 获取方式**：默认 **`frontend.skip.installnodenpm=true`**，使用本机 PATH 上的 `node` / `npm`；在无 Node 的构建机上可改为 **`-Dfrontend.skip.installnodenpm=false`**，由 `frontend-maven-plugin` 下载指定 Node 再构建。

   **若访问 `http://localhost:8080/` 出现 Whitelabel Error Page（404）：** 说明当前 classpath 下**没有** `static/index.html`。常见情况是只执行了 **`mvn spring-boot:run`**（生命周期只到 `test-compile`，**不会**执行 `prepare-package`，因此不会构建前端、也不会把 `frontend/dist` 拷进 `target/classes/static`）。请先在同一模块执行 **`mvn package -DskipTests`**（或 `mvn clean package -DskipTests`）再打 JAR 启动，或先 `package` 再 **`mvn spring-boot:run`**，使 `target/classes/static/` 存在后再访问根路径。

4. **回归**

   ```bash
   cd backend
   mvn test
   ```

**Windows**：终端里优先用 **`curl.exe`** 调试 HTTP，避免 `curl` 被解析为 `Invoke-WebRequest`。给 Vite 传 CLI 参数时，**必须**使用 npm 的双减号分界，例如 `npm run dev -- --host 0.0.0.0 --port 5174`。若多余的位置参数落到 Vite 上，可能被当成 **`[root]`** 工程目录，导致 **`/api` 代理不生效**（对 `/api/v1/*` 出现空响应体 404）。

**换端口**：后端改 `backend/src/main/resources/application.yml` 中 `server.port`；前端改 `frontend/vite.config.js` 中 `server.port`，并把 `proxy['/api'].target` 指回实际后端 base（默认 `http://localhost:8080`）。

## 免责声明（法律语义）

本产品输出仅为研发辅助信息与规则命中提示，**不替代**律师、司法机关或本单位合规/安全负责人的专业判断；任何落地前均需人工复核与测试验证。
