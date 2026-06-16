<template>
  <view class="page">
    <view class="hero">
      <text class="badge">浙江省餐饮信息</text>
      <text class="title">登录后查看联系方式</text>
      <text class="desc">手机号验证码登录，开发环境可使用固定验证码 123456</text>
    </view>

    <view class="form-card">
      <view class="field">
        <text class="field-label">手机号</text>
        <input v-model="phone" class="input" type="number" maxlength="11" placeholder="请输入手机号" />
      </view>
      <view class="field">
        <text class="field-label">验证码</text>
        <view class="code-row">
          <input v-model="code" class="input" type="number" maxlength="6" placeholder="验证码" />
          <button class="code-btn" :disabled="countdown > 0 || sending" @click="handleSendCode">
            {{ countdown > 0 ? `${countdown}s` : "获取验证码" }}
          </button>
        </view>
      </view>
      <button class="submit" :loading="loading" @click="handleLogin">登录</button>
      <text v-if="error" class="error">{{ error }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { loginBySms, sendSmsCode } from "@/api/auth";
import { setToken } from "@/api/request";

const phone = ref("");
const code = ref("123456");
const loading = ref(false);
const sending = ref(false);
const countdown = ref(0);
const error = ref("");
const redirect = ref("");
let timer: ReturnType<typeof setInterval> | null = null;

function startCountdown() {
  countdown.value = 60;
  timer = setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0 && timer) {
      clearInterval(timer);
      timer = null;
    }
  }, 1000);
}

async function handleSendCode() {
  if (!/^1\d{10}$/.test(phone.value)) {
    error.value = "请输入正确的手机号";
    return;
  }
  sending.value = true;
  error.value = "";
  try {
    await sendSmsCode(phone.value);
    uni.showToast({ title: "验证码已发送", icon: "none" });
    startCountdown();
  } catch (e) {
    error.value = e instanceof Error ? e.message : "发送失败";
  } finally {
    sending.value = false;
  }
}

async function handleLogin() {
  if (!phone.value || !code.value) {
    error.value = "请填写手机号和验证码";
    return;
  }
  loading.value = true;
  error.value = "";
  try {
    const res = await loginBySms(phone.value, code.value);
    setToken(res.data.token);
    uni.setStorageSync("user_phone", res.data.phone);
    uni.setStorageSync("user_nickname", res.data.nickname);
    uni.showToast({ title: "登录成功", icon: "success" });
    setTimeout(() => {
      if (redirect.value) {
        uni.redirectTo({ url: redirect.value });
      } else {
        uni.navigateBack();
      }
    }, 400);
  } catch (e) {
    error.value = e instanceof Error ? e.message : "登录失败";
  } finally {
    loading.value = false;
  }
}

onLoad((query) => {
  redirect.value = typeof query?.redirect === "string" ? decodeURIComponent(query.redirect) : "";
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 48rpx 32rpx;
  background: linear-gradient(180deg, #fff6ea 0%, #f7f2ea 100%);
}

.hero {
  margin-bottom: 36rpx;
}

.badge {
  display: inline-block;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(200, 121, 65, 0.12);
  color: #a85a24;
  font-size: 22rpx;
}

.title {
  display: block;
  margin-top: 20rpx;
  font-size: 44rpx;
  font-weight: 700;
  color: #2a2118;
}

.desc {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  line-height: 1.6;
  color: #7a6f63;
}

.form-card {
  padding: 32rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 16rpx 48rpx rgba(42, 33, 24, 0.08);
}

.field {
  margin-bottom: 24rpx;
}

.field-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 24rpx;
  color: #9a8f82;
}

.input {
  height: 84rpx;
  padding: 0 24rpx;
  border-radius: 16rpx;
  background: #faf5ee;
  font-size: 30rpx;
}

.code-row {
  display: flex;
  gap: 16rpx;
}

.code-row .input {
  flex: 1;
}

.code-btn {
  min-width: 200rpx;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 16rpx;
  background: #f1e7da;
  color: #a85a24;
  font-size: 26rpx;
}

.submit {
  margin-top: 12rpx;
  height: 92rpx;
  line-height: 92rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #c87941, #a85a24);
  color: #fff;
  font-size: 30rpx;
}

.error {
  display: block;
  margin-top: 16rpx;
  color: #d64545;
  font-size: 24rpx;
}
</style>
