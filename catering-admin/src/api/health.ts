import request from './request'

export interface ApiResult<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export function fetchHealth() {
  return request.get<ApiResult<{ status: string }>>('/common/health')
}
