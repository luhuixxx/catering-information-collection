<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">浙江省 · 餐饮信息</text>
      <text class="title">找店铺、招人手、转生意</text>
      <text class="subtitle">招聘 · 转让 · 出租 · 求职 · 招商加盟</text>
    </view>

    <RegionPicker v-model="region" />

    <view class="card auth-card">
      <view class="auth-row">
        <view>
          <text class="auth-label">登录状态</text>
          <text class="auth-value">{{ loggedIn ? nickname : "未登录" }}</text>
        </view>
        <button v-if="loggedIn" class="ghost-btn" @click="logout">退出</button>
        <button v-else class="primary-btn" @click="goLogin">手机号登录</button>
      </view>
      <text class="tip">登录后可查看发布信息中的联系电话</text>
    </view>

    <view class="card action-card">
      <view class="actions">
        <button class="primary-btn" @click="goPublish">发布信息</button>
        <button class="ghost-btn" @click="goMyPosts">我的发布</button>
      </view>
      <text class="tip">先保存草稿，再提交审核；审核结果在“我的发布”里查看</text>
    </view>

    <view class="card status-card">
      <text class="status-label">后端服务</text>
      <text class="status-value">{{ healthText }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import RegionPicker from "@/components/RegionPicker.vue";
import { clearToken, fetchHealth, getToken } from "@/api/request";

const healthText = ref("检测中...");
const loggedIn = ref(false);
const nickname = ref("");
const region = ref<{ cityId?: number; districtId?: number; label?: string }>({});

async function refreshHealth() {
  try {
    const res = await fetchHealth();
    healthText.value = res.data?.status ?? "unknown";
  } catch {
    healthText.value = "未连接";
  }
}

function refreshAuth() {
  loggedIn.value = !!getToken();
  nickname.value = uni.getStorageSync("user_nickname") || uni.getStorageSync("user_phone") || "已登录";
}

function goLogin() {
  uni.navigateTo({ url: "/pages/login/login" });
}

function goPublish() {
  if (!loggedIn.value) {
    goLogin();
    return;
  }
  uni.navigateTo({ url: "/pages/publish/publish" });
}

function goMyPosts() {
  if (!loggedIn.value) {
    goLogin();
    return;
  }
  uni.navigateTo({ url: "/pages/my-posts/my-posts" });
}

function logout() {
  clearToken();
  uni.removeStorageSync("user_phone");
  uni.removeStorageSync("user_nickname");
  refreshAuth();
  uni.showToast({ title: "已退出", icon: "none" });
}

onShow(() => {
  refreshHealth();
  refreshAuth();
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 48rpx 32rpx 64rpx;
  background:
    radial-gradient(circle at 10% 0%, rgba(200, 121, 65, 0.18), transparent 42%),
    linear-gradient(180deg, #fff6ea 0%, #f7f2ea 100%);
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.hero {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.eyebrow {
  font-size: 22rpx;
  letter-spacing: 0.12em;
  color: #a85a24;
}

.title {
  font-size: 48rpx;
  font-weight: 700;
  color: #2a2118;
}

.subtitle {
  font-size: 26rpx;
  color: #7a6f63;
}

.card {
  padding: 28rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 16rpx 48rpx rgba(42, 33, 24, 0.06);
}

.auth-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.auth-label,
.status-label {
  display: block;
  font-size: 22rpx;
  color: #9a8f82;
}

.auth-value,
.status-value {
  display: block;
  margin-top: 8rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #2a2118;
}

.tip {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #9a8f82;
}

.primary-btn,
.ghost-btn {
  min-width: 180rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
}

.primary-btn {
  background: linear-gradient(135deg, #c87941, #a85a24);
  color: #fff;
}

.ghost-btn {
  background: #f1e7da;
  color: #a85a24;
}
</style>
