import { request } from "./request";

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface AppLoginResult {
  token: string;
  expiresIn: number;
  userId: number;
  phone: string;
  nickname: string;
}

export function sendSmsCode(phone: string) {
  return request<{ phone: string; message: string }>({
    url: "/common/auth/sms/send",
    method: "POST",
    data: { phone },
  });
}

export function loginBySms(phone: string, code: string) {
  return request<AppLoginResult>({
    url: "/common/auth/sms/login",
    method: "POST",
    data: { phone, code },
  });
}
