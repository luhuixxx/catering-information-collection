import request from './request'
import type { ApiResult } from './types'

export interface RegionNode {
  id: number
  parentId: number
  level: number
  code?: string
  name: string
  sortNo: number
  enabled: number
  children?: RegionNode[]
}

export interface RegionSavePayload {
  parentId: number
  level: number
  code?: string
  name: string
  sortNo?: number
  enabled?: number
}

export function fetchRegionTree() {
  return request.get<ApiResult<RegionNode[]>>('/admin/regions/tree')
}

export function createRegion(payload: RegionSavePayload) {
  return request.post<ApiResult<{ id: number }>>('/admin/regions', payload)
}

export function updateRegion(id: number, payload: RegionSavePayload) {
  return request.put<ApiResult<void>>(`/admin/regions/${id}`, payload)
}

export function setRegionEnabled(id: number, enabled: boolean) {
  return request.put<ApiResult<void>>(`/admin/regions/${id}/enabled`, null, { params: { enabled } })
}

export function deleteRegion(id: number) {
  return request.delete<ApiResult<void>>(`/admin/regions/${id}`)
}
