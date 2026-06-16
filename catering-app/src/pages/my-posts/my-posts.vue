<template>
  <view class="page">
    <view class="hero">
      <text class="title">我的发布</text>
      <text class="subtitle">查看审核进度，驳回后可修改并重新提交</text>
    </view>

    <scroll-view scroll-x class="tabs-scroll">
      <view class="tabs">
        <view
          v-for="item in tabs"
          :key="item.value"
          class="tab"
          :class="{ active: status === item.value }"
          @click="switchStatus(item.value)"
        >
          {{ item.label }}
        </view>
      </view>
    </scroll-view>

    <view class="list">
      <view v-if="loading" class="loading">加载中...</view>
      <view v-else-if="posts.length === 0" class="empty">当前状态暂无数据</view>
      <view v-else v-for="post in posts" :key="post.id" class="card" @click="goEdit(post)">
        <view class="top">
          <text class="type">{{ post.postType }}</text>
          <text class="status" :class="`s-${post.status}`">{{ statusLabel(post.status) }}</text>
        </view>
        <text class="title-line">{{ post.title }}</text>
        <text v-if="post.latestRejectReason" class="reject-reason">驳回原因：{{ post.latestRejectReason }}</text>
        <view class="meta">
          <text>编号：{{ post.postNo }}</text>
          <text>创建：{{ formatTime(post.createdAt) }}</text>
        </view>
        <view class="action-line">
          <text>{{ post.status === 'APPROVED' ? `有效期至：${formatTime(post.expireAt)}` : actionHint(post.status) }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { fetchMyPosts, type MyPostItem } from "@/api/post";

const tabs = [
  { label: "全部", value: "" },
  { label: "草稿", value: "DRAFT" },
  { label: "待审核", value: "PENDING" },
  { label: "已通过", value: "APPROVED" },
  { label: "已驳回", value: "REJECTED" },
  { label: "已过期", value: "EXPIRED" },
];

const status = ref("");
const loading = ref(false);
const posts = ref<MyPostItem[]>([]);

function statusLabel(v: string) {
  const map: Record<string, string> = {
    DRAFT: "草稿",
    PENDING: "待审核",
    APPROVED: "已通过",
    REJECTED: "已驳回",
    EXPIRED: "已过期",
    OFFLINE: "已下架",
  };
  return map[v] || v;
}

function formatTime(v: string) {
  return v ? v.replace("T", " ").slice(0, 16) : "-";
}

function actionHint(v: string) {
  const map: Record<string, string> = {
    DRAFT: "点击继续编辑并提交",
    PENDING: "运营审核中，请耐心等待",
    REJECTED: "点击修改后可重新提交",
    EXPIRED: "已过期，可重新编辑发布",
    OFFLINE: "已下架",
  };
  return map[v] || "点击查看";
}

async function load() {
  loading.value = true;
  try {
    const res = await fetchMyPosts(status.value || undefined);
    posts.value = res.data;
  } catch {
    posts.value = [];
    uni.showToast({ title: "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function switchStatus(v: string) {
  status.value = v;
  load();
}

function goEdit(post: MyPostItem) {
  uni.navigateTo({ url: `/pages/publish/publish?postId=${post.id}` });
}

onShow(load);
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 42rpx 28rpx 56rpx;
  background: linear-gradient(180deg, #fff8ee 0%, #f5efe4 100%);
}

.hero .title {
  font-size: 44rpx;
  color: #2a2118;
  font-weight: 800;
}

.hero .subtitle {
  display: block;
  margin-top: 10rpx;
  color: #7a6f63;
  font-size: 24rpx;
}

.tabs-scroll {
  margin-top: 20rpx;
  white-space: nowrap;
}

.tabs {
  display: inline-flex;
  gap: 12rpx;
}

.tab {
  padding: 12rpx 20rpx;
  border-radius: 999rpx;
  background: #eadfcf;
  color: #7a6f63;
  font-size: 24rpx;
}

.tab.active {
  background: #c87941;
  color: #fff;
}

.list {
  margin-top: 22rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.card {
  border-radius: 18rpx;
  background: #fff;
  padding: 20rpx;
  box-shadow: 0 14rpx 36rpx rgba(42, 33, 24, 0.06);
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.type {
  font-size: 22rpx;
  color: #9a8f82;
}

.status {
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
}

.s-DRAFT { background: #eee4d8; color: #7a6f63; }
.s-PENDING { background: #fff0cc; color: #a97300; }
.s-APPROVED { background: #ddf4e6; color: #177245; }
.s-REJECTED { background: #ffe0df; color: #b3322a; }
.s-EXPIRED { background: #e6e6e6; color: #666; }

.title-line {
  margin-top: 10rpx;
  font-size: 30rpx;
  color: #2a2118;
  font-weight: 600;
}

.meta {
  margin-top: 10rpx;
  display: flex;
  justify-content: space-between;
  color: #9a8f82;
  font-size: 22rpx;
}

.reject-reason {
  display: block;
  margin-top: 10rpx;
  padding: 12rpx 14rpx;
  border-radius: 12rpx;
  color: #9b312b;
  background: #fff0ee;
  font-size: 24rpx;
}

.action-line {
  margin-top: 12rpx;
  color: #7a6f63;
  font-size: 23rpx;
}

.loading,
.empty {
  padding: 26rpx;
  text-align: center;
  color: #8f8578;
  font-size: 24rpx;
}
</style>
