<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">浙江省 · 餐饮信息</text>
      <text class="title">找店铺、招人手、转生意</text>
      <text class="subtitle">招聘 · 转让 · 出租 · 求职 · 招商加盟</text>
    </view>

    <RegionPicker v-model="region" />

    <view class="search-card">
      <input v-model="keyword" class="search" placeholder="搜岗位、店铺、地区关键词" confirm-type="search" @confirm="goSearch" />
      <button class="search-btn" @click="goSearch">搜索</button>
    </view>

    <view class="type-grid">
      <view v-for="item in types" :key="item.value" class="type-card" @click="goList(item.value)">
        <text class="type-name">{{ item.label }}</text>
        <text class="type-desc">{{ item.desc }}</text>
      </view>
    </view>

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

    <view class="latest">
      <view class="section-head">
        <text class="section-title">最新信息</text>
        <text class="section-link" @click="goList('RECRUIT')">查看更多</text>
      </view>
      <view v-if="latestLoading" class="empty">加载中...</view>
      <view v-else-if="latest.length === 0" class="empty">暂无已审核上架信息</view>
      <view v-for="item in latest" v-else :key="item.id" class="latest-card" @click="goDetail(item.id)">
        <text class="latest-title">{{ item.title }}</text>
        <text class="latest-summary">{{ item.summary }}</text>
        <text class="latest-meta">{{ item.cityName }} · {{ item.districtName }}</text>
      </view>
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
import { fetchPublicPosts, type PublicPostItem } from "@/api/post";

const healthText = ref("检测中...");
const loggedIn = ref(false);
const nickname = ref("");
const region = ref<{ cityId?: number; districtId?: number; label?: string }>({});
const keyword = ref("");
const latestLoading = ref(false);
const latest = ref<PublicPostItem[]>([]);
const types = [
  { label: "招聘", value: "RECRUIT", desc: "厨师、服务员、收银" },
  { label: "转让", value: "TRANSFER", desc: "餐饮店铺转让" },
  { label: "出租", value: "RENT", desc: "铺面出租" },
  { label: "求职", value: "JOB_SEEK", desc: "餐饮从业者" },
  { label: "加盟", value: "FRANCHISE", desc: "餐饮招商" },
];

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

function goList(type: string) {
  const params = [`type=${type}`];
  if (region.value.cityId) params.push(`cityId=${region.value.cityId}`);
  if (region.value.districtId) params.push(`districtId=${region.value.districtId}`);
  uni.navigateTo({ url: `/pages/list/list?${params.join("&")}` });
}

function goSearch() {
  const params = [`type=RECRUIT`];
  if (keyword.value.trim()) params.push(`keyword=${encodeURIComponent(keyword.value.trim())}`);
  if (region.value.cityId) params.push(`cityId=${region.value.cityId}`);
  uni.navigateTo({ url: `/pages/list/list?${params.join("&")}` });
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` });
}

async function loadLatest() {
  latestLoading.value = true;
  try {
    const res = await fetchPublicPosts({ cityId: region.value.cityId, page: 1, size: 5 });
    latest.value = res.data.records;
  } catch {
    latest.value = [];
  } finally {
    latestLoading.value = false;
  }
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
  loadLatest();
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

.search-card {
  display: flex;
  gap: 12rpx;
  padding: 14rpx;
  border-radius: 18rpx;
  background: #fff;
  box-shadow: 0 12rpx 34rpx rgba(42, 33, 24, 0.05);
}

.search {
  min-width: 0;
  flex: 1;
  height: 76rpx;
  padding: 0 20rpx;
  border-radius: 14rpx;
  background: #faf5ee;
  font-size: 27rpx;
}

.search-btn {
  width: 140rpx;
  height: 76rpx;
  line-height: 76rpx;
  border-radius: 14rpx;
  background: #2a2118;
  color: #fff6ea;
  font-size: 26rpx;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
}

.type-card {
  min-height: 128rpx;
  padding: 22rpx;
  border-radius: 18rpx;
  background: #fff;
  box-shadow: 0 12rpx 34rpx rgba(42, 33, 24, 0.05);
}

.type-name {
  display: block;
  color: #2a2118;
  font-size: 32rpx;
  font-weight: 800;
}

.type-desc {
  display: block;
  margin-top: 10rpx;
  color: #9a8f82;
  font-size: 23rpx;
}

.latest {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  color: #2a2118;
  font-size: 32rpx;
  font-weight: 800;
}

.section-link {
  color: #a85a24;
  font-size: 24rpx;
}

.latest-card,
.empty {
  padding: 20rpx;
  border-radius: 16rpx;
  background: #fff;
}

.latest-title,
.latest-summary,
.latest-meta {
  display: block;
}

.latest-title {
  color: #2a2118;
  font-size: 29rpx;
  font-weight: 700;
}

.latest-summary {
  margin-top: 8rpx;
  color: #66594d;
  font-size: 24rpx;
}

.latest-meta,
.empty {
  margin-top: 8rpx;
  color: #9a8f82;
  font-size: 22rpx;
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

.actions {
  display: flex;
  gap: 14rpx;
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
