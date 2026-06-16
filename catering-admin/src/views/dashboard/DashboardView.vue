<template>
  <el-card>
    <template #header>工作台</template>
    <p>管理端框架已初始化。</p>
    <p v-if="healthText">后端健康检查：{{ healthText }}</p>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchHealth } from '@/api/health'

const healthText = ref('')

onMounted(async () => {
  try {
    const { data } = await fetchHealth()
    healthText.value = data.data?.status ?? 'unknown'
  } catch {
    healthText.value = '未连接'
  }
})
</script>
