import axios from 'axios'

/** Client timeout aligned with README / application.yml app.processing.timeout-seconds (≈10s). */
const REQUEST_TIMEOUT_MS = 10_000

export const api = axios.create({
  baseURL: '/api',
  timeout: REQUEST_TIMEOUT_MS,
  headers: { 'Content-Type': 'application/json' },
})

/**
 * 若后端误用 YAML Mapper 写出「长得像 JSON 的字符串」或双重序列化，axios 默认会得到 string，
 * `payload.valid` 会变成 undefined，严格 `=== true` 会误判为不合格。此处尽力解析为对象。
 */
export function unwrapAxiosJsonBody(data) {
  if (data == null) return data
  if (typeof data !== 'string') return data
  const t = data.trim()
  if (
    t.length === 0 ||
    (!(t.startsWith('{') && t.endsWith('}')) && !(t.startsWith('[') && t.endsWith(']')))
  ) {
    return data
  }
  try {
    return JSON.parse(t)
  } catch {
    return data
  }
}

/** 与 CaseNumberValidateResponse.valid 对齐：兼容布尔、字符串、缺失字段时的结构推断（MVP）。 */
export function isCaseValidationPassed(payload) {
  const p = unwrapAxiosJsonBody(payload)
  if (!p || typeof p !== 'object') return false

  const v = p.valid
  if (v === true) return true
  if (v === false) return false
  if (typeof v === 'string') {
    const s = v.trim().toLowerCase()
    if (s === 'true' || s === '1') return true
    if (s === 'false' || s === '0' || s === '') return false
  }
  if (typeof v === 'number') {
    if (v === 1) return true
    if (v === 0) return false
  }

  // valid 缺失时的保守推断（仍可能被 wrong Content-Type / 字符串 body 触发）
  const rc = p.reasonCode
  if (rc != null && String(rc).length > 0) return false
  const norm = p.normalized
  if (norm != null && String(norm).trim().length > 0) return true
  const refs = p.ruleRefs
  if (Array.isArray(refs) && refs.some((r) => r?.ruleId === 'CN-YEAR-SP-001')) return true

  return false
}

api.interceptors.response.use(
  (response) => {
    response.data = unwrapAxiosJsonBody(response.data)
    return response
  },
  (error) => {
    const d = error.response?.data
    if (d !== undefined && error.response) {
      error.response.data = unwrapAxiosJsonBody(d)
    }
    return Promise.reject(error)
  },
)

export function formatTimeoutHint(err) {
  if (err?.code === 'ECONNABORTED' || err?.message?.includes('timeout')) {
    return '请求超时（前端约 10s）；可缩小输入后重试。'
  }
  const status = err?.response?.status
  if (status === 504) {
    return err?.response?.data?.message || '服务器处理超时（HTTP 504）。'
  }
  if (status === 422) {
    return err?.response?.data?.message || '文书抽取失败（HTTP 422）。'
  }
  if (status === 400) {
    return err?.response?.data?.message || '请求格式错误（HTTP 400）。'
  }
  return err?.message || '请求失败。'
}
