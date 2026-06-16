<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">餐饮信息平台</div>
      <el-menu :default-active="activeMenu" router>
        <el-menu-item index="/">工作台</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>管理端</span>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.aside {
  border-right: 1px solid var(--el-border-color);
}

.logo {
  padding: 20px 16px;
  font-weight: 600;
}

.header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--el-border-color);
}
</style>
