<template>
  <view class="page">
    <view class="head">
      <text class="title">我的收藏</text>
      <text class="sub">把正在比较的信息先收好，晚点再联系</text>
    </view>

    <view class="list">
      <view v-if="loading" class="state">加载中...</view>
      <view v-else-if="items.length === 0" class="state">还没有收藏的信息</view>
      <view v-for="item in items" v-else :key="item.favoriteId" class="card" @click="goDetail(item.post.id)">
        <image v-if="item.post.coverImage" class="cover" :src="item.post.coverImage" mode="aspectFill" />
        <view class="content">
          <view class="line">
            <text class="badge">{{ labelOf(item.post.postType) }}</text>
            <text class="time">{{ dateText(item.favoritedAt) }}</text>
          </view>
          <text class="post-title">{{ item.post.title }}</text>
          <text class="summary">{{ item.post.summary }}</text>
          <text class="meta">{{ item.post.cityName }} · {{ item.post.districtName }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { fetchFavorites, type FavoritePostItem } from "@/api/post";
import { isLoggedIn } from "@/api/request";

const loading = ref(false);
const items = ref<FavoritePostItem[]>([]);

function labelOf(type: string) {
  const map: Record<string, string> = {
    RECRUIT: "招聘",
    TRANSFER: "转让",
    RENT: "出租",
    JOB_SEEK: "求职",
    FRANCHISE: "招商加盟",
  };
  return map[type] || type;
}

function dateText(value: string) {
  return value ? `收藏于 ${value.replace("T", " ").slice(5, 16)}` : "";
}

async function load() {
  if (!isLoggedIn()) {
    uni.navigateTo({ url: `/pages/login/login?redirect=${encodeURIComponent("/pages/favorites/favorites")}` });
    return;
  }
  loading.value = true;
  try {
    const res = await fetchFavorites();
    items.value = res.data;
  } catch (e) {
    uni.showToast({ title: e instanceof Error ? e.message : "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` });
}

onShow(load);
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 36rpx 28rpx 56rpx;
  background: #f7f2ea;
}

.head {
  margin-bottom: 22rpx;
}

.title {
  display: block;
  color: #2a2118;
  font-size: 42rpx;
  font-weight: 800;
}

.sub {
  display: block;
  margin-top: 8rpx;
  color: #8f8578;
  font-size: 25rpx;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.card {
  display: flex;
  gap: 18rpx;
  padding: 18rpx;
  border-radius: 18rpx;
  background: #fff;
  box-shadow: 0 12rpx 30rpx rgba(42, 33, 24, 0.05);
}

.cover {
  width: 144rpx;
  height: 144rpx;
  border-radius: 14rpx;
  background: #eadfcf;
  flex-shrink: 0;
}

.content {
  min-width: 0;
  flex: 1;
}

.line {
  display: flex;
  justify-content: space-between;
  gap: 12rpx;
}

.badge {
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
  color: #a85a24;
  background: #f1e7da;
  font-size: 21rpx;
}

.time,
.meta {
  color: #9a8f82;
  font-size: 22rpx;
}

.post-title {
  display: block;
  margin-top: 8rpx;
  color: #2a2118;
  font-size: 30rpx;
  font-weight: 700;
}

.summary {
  display: block;
  margin-top: 6rpx;
  color: #66594d;
  font-size: 24rpx;
}

.meta {
  display: block;
  margin-top: 12rpx;
}

.state {
  padding: 60rpx 24rpx;
  border-radius: 18rpx;
  text-align: center;
  color: #8f8578;
  background: #fff;
  font-size: 26rpx;
}
</style>
