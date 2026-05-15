# Standard library payloads（可被程序加载）

请将下列文件的 `version / maintainer / source`（或 `synthetic-demo` 语义字段）视作真源片段；枚举 id 变更需同步 [docs/golden-fixtures.md](../../docs/golden-fixtures.md) 与 [contracts/openapi.yaml](../../contracts/openapi.yaml)。

| 文件 | 说明 |
|------|------|
| `stdlib-index.v0_1_0_demo.yaml` | 聚合索引（可选用作启动加载清单） |
| `rules-case-number-demo.yaml` | 案号规则 **synthetic-demo** + 公开可查**参考文献书目** |
| `compliance-rules.v0_1_0_demo.yaml` | 合规离线规则占位（正则/字面量） |
| `document-pipelines.v0_1_0_demo.yaml` | MVP 冻结文书管线 `civil-judgment-v1`（字段名对齐 OpenAPI） |

**加载顺序（建议，无运行时硬依赖）：** 先案号规则 `rules-case-number-demo.yaml`，再合规 `compliance-rules.v0_1_0_demo.yaml`，最后文书管线 `document-pipelines.v0_1_0_demo.yaml`；汇总见 `stdlib-index*.yaml` 中的 `loadSequenceNotes_zh`。

每条库文件须保留：

- `version`
- `maintainer`
- `source`: 固定值 **`synthetic-demo`** 或可核查 URI 入口（不得在库内捏造法条正文）

MVP 断言以 [docs/golden-fixtures.md](../../docs/golden-fixtures.md) 为准：当前 **20** 个 `scenarioId`，路径模式 `fixtures/case-number|document-extract|compliance/**/*.{request,expected}.json` 与配对 `.txt`。
