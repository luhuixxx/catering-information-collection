import request from './request'
import type { ApiResult } from './types'

export interface AdminLoginResult {
  token: string
  expiresIn: number
  userId: number
  username: string
  displayName: string
  roles: string[]
}

export function adminLogin(username: string, password: string) {
  return request.post<ApiResult<AdminLoginResult>>('/admin/auth/login', { username, password })
}
