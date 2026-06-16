import { request } from "./request";

export interface MyPostItem {
  id: string;
  postNo: string;
  postType: string;
  status: string;
  title: string;
  coverImage: string;
  createdAt: string;
  expireAt: string;
}

export interface UploadTokenData {
  provider: string;
  uploadUrl: string;
  uploadMethod: string;
  formData: Record<string, string>;
  objectKey: string;
  objectUrl: string;
  expireAt: number;
}

export function saveRecruitDraft(payload: Record<string, unknown>) {
  return request<{ postId: string }>({ url: "/app/posts/recruit/draft", method: "POST", data: payload });
}

export function saveTransferDraft(payload: Record<string, unknown>) {
  return request<{ postId: string }>({ url: "/app/posts/transfer/draft", method: "POST", data: payload });
}

export function updateRecruitDraft(postId: string, payload: Record<string, unknown>) {
  return request<void>({ url: `/app/posts/${postId}/recruit`, method: "PUT", data: payload });
}

export function updateTransferDraft(postId: string, payload: Record<string, unknown>) {
  return request<void>({ url: `/app/posts/${postId}/transfer`, method: "PUT", data: payload });
}

export function submitPost(postId: string) {
  return request<void>({ url: `/app/posts/${postId}/submit`, method: "POST" });
}

export function fetchMyPosts(status?: string) {
  const q = status ? `?status=${encodeURIComponent(status)}` : "";
  return request<MyPostItem[]>({ url: `/app/posts/mine${q}`, method: "GET" });
}

export function fetchEditablePost(postId: string) {
  return request<Record<string, unknown>>({ url: `/app/posts/${postId}/edit`, method: "GET" });
}

export function fetchUploadToken(fileName: string, contentType: string) {
  return request<UploadTokenData>({
    url: `/common/upload-token?fileName=${encodeURIComponent(fileName)}&contentType=${encodeURIComponent(contentType)}`,
    method: "GET",
  });
}

export async function uploadImageToMinio(filePath: string): Promise<string> {
  return await new Promise<string>((resolve, reject) => {
    uni.uploadFile({
      url: "/api/common/upload",
      filePath,
      name: "file",
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          try {
            const body = JSON.parse(res.data as unknown as string) as {
              code: number;
              data?: { url?: string };
              message?: string;
            };
            if (body.code === 0 && body.data?.url) {
              resolve(body.data.url);
            } else {
              reject(new Error(body.message || "上传失败"));
            }
          } catch {
            reject(new Error("上传失败（响应解析错误）"));
          }
        } else {
          reject(new Error(`上传失败(${res.statusCode})`));
        }
      },
      fail: (err) => reject(err),
    });
  });
}

