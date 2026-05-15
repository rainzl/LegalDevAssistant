<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api, formatTimeoutHint, isCaseValidationPassed } from './api/client'

const tab = ref('case')

const caseInput = ref('')
const caseResult = ref(null)
const caseLoading = ref(false)

const docContent = ref('')
const docResult = ref(null)
const docLoading = ref(false)

const complianceSource = ref('')
const complianceResult = ref(null)
const complianceLoading = ref(false)

const pageMeta = computed(() => {
  switch (tab.value) {
    case 'doc':
      return {
        title: '文书抽取',
        subtitle: '单管线 civil-judgment-v1 — 结构化抽取民事判决书字段（参赛 MVP 演示）',
      }
    case 'compliance':
      return {
        title: '合规扫描',
        subtitle: '本地规则扫描代码或文本片段，输出 findings（演示）',
      }
    default:
      return {
        title: '案号校验',
        subtitle: '粘贴候选案号字符串，校验格式与规则库（演示）',
      }
  }
})

/** 案号合格判定：兼容 valid 非布尔或响应体为字符串解析后的缺口（见 api/client.js）。 */
const caseValidationPassed = computed(() => isCaseValidationPassed(caseResult.value))

/** ruleRefs[].ruleId — API 响应不含 _meta；仅用于结果展示。 */
const caseRuleIds = computed(() => {
  const refs = caseResult.value?.ruleRefs
  if (!Array.isArray(refs)) return []
  return refs.map((r) => r?.ruleId).filter(Boolean)
})

const docExtract = computed(() => {
  const ex = docResult.value?.extract
  return ex && typeof ex === 'object' ? ex : null
})

/** 文书抽取 422：ExtractErrorResponse */
const docExtractError = computed(() => {
  const r = docResult.value
  if (!r || typeof r !== 'object' || r.extract) return null
  if (r.errorCode != null && String(r.errorCode).length > 0) return r
  return null
})

const complianceFindings = computed(() => {
  const f = complianceResult.value?.findings
  return Array.isArray(f) ? f : []
})

const complianceVerdictLine = computed(() => {
  const list = complianceFindings.value
  if (list.length === 0) {
    return '合规扫描结果：零命中（findings 为空，演示）。'
  }
  const hasDet = list.some((x) => x.severity === 'deterministic')
  const hasSus = list.some((x) => x.severity === 'suspicious')
  if (hasDet && hasSus) {
    return '合规扫描结果：存在确定级与可疑级命中（演示）。'
  }
  if (hasDet) {
    return '合规扫描结果：存在确定级命中（演示）。'
  }
  if (hasSus) {
    return '合规扫描结果：存在可疑级命中；无确定级命中（演示）。'
  }
  return '合规扫描结果：已返回 findings（演示）。'
})

async function validateCase() {
  caseLoading.value = true
  caseResult.value = null
  try {
    const { data } = await api.post('/v1/case-number/validate', {
      candidate: caseInput.value ?? '',
    })
    caseResult.value = data
  } catch (e) {
    ElMessage.error(formatTimeoutHint(e))
  } finally {
    caseLoading.value = false
  }
}

async function extractDoc() {
  docLoading.value = true
  docResult.value = null
  try {
    const { data } = await api.post('/v1/document/extract', {
      pipelineId: 'civil-judgment-v1',
      schemaVersion: '2026-05-13',
      content: docContent.value ?? '',
      contentType: 'text/plain',
      encoding: 'UTF-8',
    })
    docResult.value = data
  } catch (e) {
    if (e.response?.status === 422) {
      docResult.value = e.response.data
      return
    }
    ElMessage.error(formatTimeoutHint(e))
  } finally {
    docLoading.value = false
  }
}

async function scanCompliance() {
  complianceLoading.value = true
  complianceResult.value = null
  try {
    const { data } = await api.post('/v1/compliance/scan', {
      source: complianceSource.value ?? '',
    })
    complianceResult.value = data
  } catch (e) {
    ElMessage.error(formatTimeoutHint(e))
  } finally {
    complianceLoading.value = false
  }
}

function complianceSummaryText() {
  const head = complianceVerdictLine.value
  const f = complianceFindings.value
  if (!f.length) return head
  const lines = f.map(
    (x) => `- ${x.ruleId} (${x.severity}) ${x.message || ''}`.trim(),
  )
  return [head, '', ...lines].join('\n')
}

async function copyComplianceSummary() {
  const text = complianceSummaryText()
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制摘要到剪贴板')
  } catch {
    ElMessage.warning('复制失败：浏览器权限或环境限制')
  }
}

function pretty(obj) {
  try {
    return JSON.stringify(obj, null, 2)
  } catch {
    return String(obj)
  }
}

function onMenuSelect(key) {
  tab.value = key
}
</script>

<template>
  <el-container class="app-shell">
    <el-aside class="app-aside" width="224px">
      <div class="aside-brand">
        <div class="aside-brand-title">法律 AI 助手</div>
        <div class="aside-brand-sub">Legal dev offline MVP</div>
      </div>
      <el-menu
        class="aside-menu"
        :default-active="tab"
        background-color="transparent"
        :text-color="'var(--legal-sidebar-text)'"
        :active-text-color="'#ffffff'"
        @select="onMenuSelect"
      >
        <el-menu-item index="case">
          <span>案号校验</span>
        </el-menu-item>
        <el-menu-item index="doc">
          <span>文书抽取</span>
        </el-menu-item>
        <el-menu-item index="compliance">
          <span>合规扫描</span>
        </el-menu-item>
      </el-menu>
      <div class="aside-foot">
        <p>离线三件套演示</p>
        <p class="aside-foot-version">fixtures / 同源 /api</p>
      </div>
    </el-aside>

    <el-container class="app-main-wrap">
      <el-main class="app-main">
        <header class="page-header">
          <h1 class="page-title">{{ pageMeta.title }}</h1>
          <p class="page-subtitle">{{ pageMeta.subtitle }}</p>
        </header>

        <section v-show="tab === 'case'" class="workspace-section">
          <el-card class="panel-card" shadow="never">
            <p class="panel-hint">
              本页为<strong>匿名试用</strong>：无登录、无 API Key、无运行时公网大模型。案号规则来自仓库内
              <code>fixtures/stdlib</code>；请求经同源 <code>/api/v1/*</code> 在后端进程内处理。
            </p>
            <el-input
              v-model="caseInput"
              type="textarea"
              :rows="10"
              placeholder="粘贴候选案号字符串（演示数据）..."
              class="panel-input"
            />
            <div class="panel-actions">
              <el-button type="primary" :loading="caseLoading" @click="validateCase">校验</el-button>
            </div>
          </el-card>
          <el-card v-if="caseResult" class="result-card" shadow="never">
            <template #header>结果</template>
            <div class="case-result">
              <div
                class="case-result-verdict"
                :class="
                  caseValidationPassed ? 'case-result-verdict--pass' : 'case-result-verdict--fail'
                "
              >
                校验结果：{{ caseValidationPassed ? '合格' : '不合格' }}
              </div>
              <dl class="case-result-dl">
                <template v-if="caseResult.normalized != null && String(caseResult.normalized).length">
                  <dt>归一化（normalized）</dt>
                  <dd>
                    <code class="case-result-code">{{ caseResult.normalized }}</code>
                  </dd>
                </template>
                <template v-if="!caseValidationPassed && caseResult.reasonCode">
                  <dt>原因码（reasonCode）</dt>
                  <dd>
                    <code class="case-result-code">{{ caseResult.reasonCode }}</code>
                  </dd>
                </template>
                <template v-if="!caseValidationPassed && caseResult.message">
                  <dt>说明（message）</dt>
                  <dd class="case-result-message">{{ caseResult.message }}</dd>
                </template>
                <template v-if="caseRuleIds.length">
                  <dt>规则引用（ruleId）</dt>
                  <dd>
                    <ul class="case-result-rules">
                      <li v-for="rid in caseRuleIds" :key="rid">
                        <code class="case-result-code">{{ rid }}</code>
                      </li>
                    </ul>
                  </dd>
                </template>
              </dl>
            </div>
          </el-card>
        </section>

        <section v-show="tab === 'doc'" class="workspace-section">
          <el-card class="panel-card" shadow="never">
            <p class="panel-hint">
              管线固定：<code>civil-judgment-v1</code> / <code>2026-05-13</code>；正文为
              <code>text/plain</code>、<code>UTF-8</code>。<strong>不向公网上传粘贴正文</strong>即可完成处理（仍建议在脱敏/合成数据上演示）。
            </p>
            <el-input
              v-model="docContent"
              type="textarea"
              :rows="14"
              placeholder="粘贴民事判决书演示正文（text/plain，UTF-8）..."
              class="panel-input"
            />
            <div class="panel-actions">
              <el-button type="primary" :loading="docLoading" @click="extractDoc">抽取</el-button>
            </div>
          </el-card>
          <el-card v-if="docResult" class="result-card" shadow="never">
            <template #header>结果</template>
            <div v-if="docExtract" class="structured-result">
              <div class="case-result-verdict case-result-verdict--pass">文书抽取结果：成功</div>
              <dl class="case-result-dl">
                <dt>审理法院（courtNameSnippet）</dt>
                <dd>
                  <code class="case-result-code">{{ docExtract.courtNameSnippet }}</code>
                </dd>
                <dt>案号（caseNumberRaw）</dt>
                <dd>
                  <code class="case-result-code">{{ docExtract.caseNumberRaw }}</code>
                </dd>
                <dt>案由（causeSnippet）</dt>
                <dd>{{ docExtract.causeSnippet }}</dd>
                <dt>文书日期（judgementDateISO）</dt>
                <dd>
                  <code class="case-result-code">{{
                    docExtract.judgementDateISO != null && docExtract.judgementDateISO !== ''
                      ? docExtract.judgementDateISO
                      : '（无）'
                  }}</code>
                </dd>
              </dl>
            </div>
            <div v-else-if="docExtractError" class="structured-result">
              <div class="case-result-verdict case-result-verdict--fail">
                文书抽取结果：失败（HTTP 422）
              </div>
              <dl class="case-result-dl">
                <dt>错误码（errorCode）</dt>
                <dd>
                  <code class="case-result-code">{{ docExtractError.errorCode }}</code>
                </dd>
                <dt>说明（message）</dt>
                <dd class="case-result-message">{{ docExtractError.message }}</dd>
              </dl>
            </div>
            <pre v-else class="result-pre">{{ pretty(docResult) }}</pre>
          </el-card>
        </section>

        <section v-show="tab === 'compliance'" class="workspace-section">
          <el-card class="panel-card" shadow="never">
            <p class="panel-hint">
              合规条目来自仓库内标准库快照；粘贴内容经同源 API 在后端匹配本地规则。
            </p>
            <el-input
              v-model="complianceSource"
              type="textarea"
              :rows="14"
              placeholder="粘贴代码或文本片段（演示）..."
              class="panel-input"
            />
            <div class="panel-actions panel-actions--wrap">
              <el-button type="primary" :loading="complianceLoading" @click="scanCompliance">扫描</el-button>
              <el-button :disabled="!complianceResult" @click="copyComplianceSummary">复制摘要</el-button>
            </div>
          </el-card>
          <el-card v-if="complianceResult" class="result-card" shadow="never">
            <template #header>结果</template>
            <div class="structured-result">
              <div
                class="case-result-verdict"
                :class="
                  complianceFindings.length === 0
                    ? 'case-result-verdict--pass'
                    : 'case-result-verdict--warn'
                "
              >
                {{ complianceVerdictLine }}
              </div>
              <template v-if="complianceFindings.length">
                <p class="compliance-findings-label">命中条目（findings）</p>
                <ul class="compliance-findings-list">
                  <li v-for="(f, idx) in complianceFindings" :key="idx" class="compliance-finding-item">
                    <span class="compliance-finding-severity">{{ f.severity }}</span>
                    <code class="case-result-code">{{ f.ruleId }}</code>
                    <span class="compliance-finding-msg">{{ f.message }}</span>
                  </li>
                </ul>
              </template>
              <p v-else class="case-result-message compliance-empty-hint">当前规则集未命中任何条目。</p>
            </div>
          </el-card>
        </section>

        <el-alert
          class="timeout-alert"
          type="info"
          show-icon
          :closable="false"
          title="请求超时"
          description="前端单次 HTTP 超时约 10s；后端预算见 application.yml 的 app.processing.timeout-seconds 与 README 声明。"
        />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: var(--legal-main-bg);
}

.app-aside {
  display: flex;
  flex-direction: column;
  background: var(--legal-sidebar-bg);
  color: #fff;
  border-right: 1px solid var(--legal-sidebar-border);
  box-sizing: border-box;
}

.aside-brand {
  padding: 1.5rem 1.25rem;
  border-bottom: 1px solid var(--legal-sidebar-border);
}

.aside-brand-title {
  font-size: 1.125rem;
  font-weight: 600;
  line-height: 1.35;
}

.aside-brand-sub {
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: var(--legal-sidebar-muted);
  line-height: 1.4;
}

.aside-menu {
  flex: 1;
  border-right: none !important;
  padding: 1rem 0.75rem;
}

.aside-menu :deep(.el-menu-item) {
  border-radius: 0.5rem;
  margin-bottom: 0.5rem;
  height: auto;
  line-height: 1.4;
  padding: 0.75rem 1rem !important;
}

.aside-menu :deep(.el-menu-item:hover) {
  background: var(--legal-sidebar-hover) !important;
}

.aside-menu :deep(.el-menu-item.is-active) {
  background: var(--legal-sidebar-active) !important;
}

.aside-foot {
  padding: 1rem 1.25rem;
  border-top: 1px solid var(--legal-sidebar-border);
  font-size: 0.75rem;
  color: var(--legal-sidebar-muted);
  line-height: 1.5;
}

.aside-foot-version {
  margin-top: 0.25rem;
  opacity: 0.95;
}

.app-main-wrap {
  flex-direction: column;
  min-width: 0;
}

.app-main {
  padding: 2rem 2rem 2.5rem;
  box-sizing: border-box;
}

.page-header {
  margin-bottom: 1.5rem;
}

.page-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--legal-heading);
  line-height: 1.3;
}

.page-subtitle {
  margin: 0.5rem 0 0;
  font-size: 0.9375rem;
  color: var(--legal-body-muted);
  line-height: 1.6;
  max-width: 52rem;
}

.workspace-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.panel-card {
  border: 1px solid var(--legal-card-border);
  border-radius: 0.5rem;
  background: #fff;
}

.panel-card :deep(.el-card__body) {
  padding: 1.25rem 1.5rem;
}

.panel-hint {
  margin: 0 0 1rem;
  font-size: 0.875rem;
  color: var(--legal-body-muted);
  line-height: 1.65;
}

.panel-input :deep(textarea) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
  font-size: 0.8125rem;
}

.panel-actions {
  margin-top: 1rem;
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.panel-actions--wrap {
  align-items: center;
}

.result-card {
  border: 1px solid var(--legal-card-border);
  border-radius: 0.5rem;
  background: #fff;
}

.result-card :deep(.el-card__header) {
  font-weight: 600;
  color: var(--legal-heading);
  border-bottom-color: var(--legal-card-border);
}

.result-card :deep(.el-card__body) {
  padding: 1rem 1.25rem;
}

.result-pre {
  margin: 0;
  white-space: pre-wrap;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
  font-size: 0.8125rem;
  line-height: 1.5;
}

.case-result-verdict {
  font-size: 1.25rem;
  font-weight: 600;
  line-height: 1.35;
  margin-bottom: 1rem;
}

.case-result-verdict--pass {
  color: var(--el-color-success);
}

.case-result-verdict--fail {
  color: var(--el-color-danger);
}

.case-result-verdict--warn {
  color: var(--el-color-warning);
}

.case-result-dl {
  margin: 0;
}

.case-result-dl dt {
  margin: 0.75rem 0 0.25rem;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.02em;
  color: var(--legal-body-muted);
}

.case-result-dl dt:first-child {
  margin-top: 0;
}

.case-result-dl dd {
  margin: 0;
  font-size: 0.9375rem;
  color: var(--legal-heading);
  line-height: 1.55;
}

.case-result-code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
  font-size: 0.875rem;
  padding: 0.125rem 0.35rem;
  border-radius: 0.25rem;
  background: var(--legal-main-bg);
  border: 1px solid var(--legal-card-border);
}

.case-result-message {
  font-size: 0.875rem;
  color: var(--legal-body-muted);
}

.case-result-rules {
  margin: 0;
  padding-left: 1.25rem;
}

.case-result-rules li {
  margin: 0.25rem 0;
}

.structured-result {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.compliance-findings-label {
  margin: 0.75rem 0 0.35rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--legal-body-muted);
}

.compliance-findings-list {
  margin: 0;
  padding-left: 1.1rem;
}

.compliance-finding-item {
  margin: 0.35rem 0;
  line-height: 1.5;
}

.compliance-finding-severity {
  display: inline-block;
  min-width: 6.5rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--legal-body-muted);
}

.compliance-finding-msg {
  margin-left: 0.35rem;
  font-size: 0.875rem;
  color: var(--legal-heading);
}

.compliance-empty-hint {
  margin: 0.25rem 0 0;
}

.timeout-alert {
  margin-top: 1.5rem;
  border-radius: 0.5rem;
  border: 1px solid var(--legal-accent-border);
  background: var(--legal-accent-surface);
}

.timeout-alert :deep(.el-alert__title) {
  color: var(--legal-accent-heading);
}

.timeout-alert :deep(.el-alert__description) {
  color: var(--legal-accent-body);
}
</style>

<style>
html,
body,
#app {
  margin: 0;
}

code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
  font-size: 0.95em;
}
</style>
