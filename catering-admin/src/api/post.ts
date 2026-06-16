import request from './request'
import type { ApiResult } from './types'

export interface PendingPost {
  id: number
  postNo: string
  postType: string
  title: string
  cityId: number
  districtId: number
  publisherUserId: number
  createdAt: string
}

export function fetchPendingPosts(postType?: string) {
  return request.get<ApiResult<PendingPost[]>>('/admin/posts/pending', { params: { postType } })
}

export function fetchPendingPostDetail(postId: number) {
  return request.get<ApiResult<Record<string, unknown>>>(`/admin/posts/${postId}/detail`)
}

export function auditPost(postId: number, action: 'APPROVE' | 'REJECT', reasonText?: string) {
  return request.post<ApiResult<Record<string, unknown>>>(`/admin/posts/${postId}/audit`, {
    action,
    reasonCode: action,
    reasonText: reasonText || '',
  })
}
