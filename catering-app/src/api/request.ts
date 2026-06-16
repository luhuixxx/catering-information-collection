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
        "Content-Type": "application/json",
        ...(options.header || {}),
        Authorization: token ? `Bearer ${token}` : "",
      },
      success: (res) => {
        const body = res.data as ApiResult<T>;
        if (body && typeof body.code === "number" && body.code !== 0) {
          reject(new Error(body.message || "请求失败"));
          return;
        }
        resolve(body);
      },
      fail: (err) => reject(err),
    });
  });
}

export function fetchHealth() {
  return request<{ status: string }>({ url: "/common/health", method: "GET" });
}

export function getToken() {
  return uni.getStorageSync("token") as string;
}

export function setToken(token: string) {
  uni.setStorageSync("token", token);
}

export function clearToken() {
  uni.removeStorageSync("token");
}

export function isLoggedIn() {
  return !!getToken();
}
