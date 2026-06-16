const BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export function request<T>(options: UniApp.RequestOptions): Promise<ApiResult<T>> {
  const token = uni.getStorageSync("token");
  return new Promise((resolve, reject) => {
    uni.request({
      ...options,
      url: `${BASE_URL}${options.url}`,
      header: {
        ...(options.header || {}),
        Authorization: token ? `Bearer ${token}` : "",
      },
      success: (res) => resolve(res.data as ApiResult<T>),
      fail: reject,
    });
  });
}

export function fetchHealth() {
  return request<{ status: string }>({ url: "/common/health", method: "GET" });
}
