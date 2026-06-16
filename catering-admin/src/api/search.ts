import request from './request'
import type { ApiResult } from './types'

export interface EsHealth {
  baseUrl: string
  indexAlias: string
  available: boolean
  clusterStatus: string
  indexExists?: boolean
  documentCount?: number
  message?: string
  indexMessage?: string
  synced?: number
}

export function fetchEsHealth() {
  return request.get<ApiResult<EsHealth>>('/admin/search/es/health')
}

export function rebuildEsIndex() {
  return request.post<ApiResult<EsHealth>>('/admin/search/es/rebuild')
}
