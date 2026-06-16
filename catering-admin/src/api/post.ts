import request from './request'
import type { ApiResult } from './types'

export interface PendingPost {
  id: string
  postNo: string
  postType: string
  status: string
  title: string
  cityId: number
  districtId: number
  publisherUserId: number
  createdAt: string
}

export interface ManagedPostItem {
  id: string
  postNo: string
  postType: string
  title: string
  cityName: string
  districtName: string
  summary: string
  isTop: number
  topUntil: string
  createdAt: string
  expireAt: string
}

export interface ManagedPostPage {
  total: number
  page: number
  size: number
  records: ManagedPostItem[]
}

export interface ReportItem {
  id: string
  postId: string
  postNo: string
  postTitle: string
  postStatus: string
  publisherUserId: string
  reporterUserId: string
  reporterPhone: string
  reason: string
  description: string
  evidenceImage: string
  status: string
  handledAction: string
  handledNote: string
  handledAt: string
  createdAt: string
}

export interface AppUserItem {
  id: string
  phone: string
  nickname: string
  violationCount: number
  bannedUntil: string
  banReason: string
  lastLoginAt: string
  createdAt: string
  postCount: number
  reportCount: number
  banned: boolean
}

export function fetchPendingPosts(postType?: string) {
  return request.get<ApiResult<PendingPost[]>>('/admin/posts/pending', { params: { postType } })
}

export function fetchPendingPostDetail(postId: string) {
  return request.get<ApiResult<Record<string, unknown>>>(`/admin/posts/${postId}/detail`)
}

export function auditPost(postId: string, action: 'APPROVE' | 'REJECT', reasonCode?: string, reasonText?: string) {
  return request.post<ApiResult<Record<string, unknown>>>(`/admin/posts/${postId}/audit`, {
    action,
    reasonCode: reasonCode || '',
    reasonText: reasonText || '',
  })
}

export function fetchManagedPosts(params: Record<string, string | number | undefined>) {
  return request.get<ApiResult<ManagedPostPage>>('/admin/post-manage', { params })
}

export function fetchManagedPostDetail(postId: string) {
  return request.get<ApiResult<Record<string, unknown>>>(`/admin/post-manage/${postId}`)
}

export function topManagedPost(postId: string, topDays = 7) {
  return request.post<ApiResult<void>>(`/admin/post-manage/${postId}/top`, { topDays })
}

export function cancelTopManagedPost(postId: string) {
  return request.post<ApiResult<void>>(`/admin/post-manage/${postId}/top/cancel`)
}

export function fetchReports(params: Record<string, string | number | undefined>) {
  return request.get<ApiResult<{ total: number; page: number; size: number; records: ReportItem[] }>>(
    '/admin/governance/reports',
    { params },
  )
}

export function handleReport(reportId: string, action: 'IGNORE' | 'OFFLINE' | 'BAN', note = '', banDays = 7) {
  return request.post<ApiResult<void>>(`/admin/governance/reports/${reportId}/handle`, { action, note, banDays })
}

export function fetchAppUsers(params: Record<string, string | number | undefined>) {
  return request.get<ApiResult<{ total: number; page: number; size: number; records: AppUserItem[] }>>(
    '/admin/governance/users',
    { params },
  )
}

export function banAppUser(userId: string, payload: { banDays: number; reason: string; offlinePosts: boolean }) {
  return request.post<ApiResult<void>>(`/admin/governance/users/${userId}/ban`, payload)
}

export function unbanAppUser(userId: string) {
  return request.post<ApiResult<void>>(`/admin/governance/users/${userId}/unban`)
}
