import { request } from "./request";

export function saveRecruitDraft(payload: Record<string, unknown>) {
  return request<{ postId: number }>({ url: "/app/posts/recruit/draft", method: "POST", data: payload });
}

export function saveTransferDraft(payload: Record<string, unknown>) {
  return request<{ postId: number }>({ url: "/app/posts/transfer/draft", method: "POST", data: payload });
}

export function submitPost(postId: number) {
  return request<void>({ url: `/app/posts/${postId}/submit`, method: "POST" });
}

