# 窗口 G：测试与缺陷记录（frontend）

## 与黄金用例的关系

本项目 **Scenario S1/S2/S3 的 POST 契约断言** 在 `backend` 的 `MvpGoldenFixturesIT` 中执行（见 [backend/docs/window-g-tests-and-bugs.md](../../backend/docs/window-g-tests-and-bugs.md)）。前端职责为 UI 与 `npm run build` 产物可构建。

## 构建验证

- 命令：`cd frontend && npm run build`
- 工具链：`vite build`（[package.json](../package.json) `scripts.build`）
- 本轮结果：通过（仅有 bundle 体积提示，非错误）。

## 粗检：日志 / 配置与完整粘贴正文

- **源码**（`frontend/src`）：未发现 `console.*`、`localStorage` / `sessionStorage` 对粘贴正文做持久化或调试输出。
- **HTTP 客户端**：[src/api/client.js](../src/api/client.js) 仅配置 `baseURL`、`timeout`（10s，与 README 对齐）、`Content-Type`；无请求/响应 body 的拦截器日志。
- **硬编码密钥模式**：未在 `frontend/src` 发现 API Key、Bearer token 常量等；依赖为公开 npm 包（`vue`、`axios`、`element-plus`）。
- **建议**：生产部署时继续避免在错误上报 SDK 中附带完整用户输入；若未来增加开发态 debug 日志，应用环境变量门禁。

## 本轮由测试发现的「前端专属」缺陷

无。本轮失败均在后端标准库 YAML 绑定（已随 `StdlibBootstrap` 修复）。
