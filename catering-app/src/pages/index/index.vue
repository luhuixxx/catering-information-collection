<template>
  <view class="content">
    <view class="text-area">
      <text class="title">餐饮信息采集平台</text>
      <text class="subtitle">用户端 H5 框架已初始化</text>
      <text class="health" v-if="healthText">后端：{{ healthText }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { fetchHealth } from "@/api/request";

const healthText = ref("");

onMounted(async () => {
  try {
    const res = await fetchHealth();
    healthText.value = res.data?.status ?? "unknown";
  } catch {
    healthText.value = "未连接";
  }
});
</script>

<style>
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 32rpx;
}

.text-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.title {
  font-size: 40rpx;
  font-weight: 600;
  color: #333;
}

.subtitle,
.health {
  font-size: 28rpx;
  color: #8f8f94;
}
</style>
