<template>
  <el-container class="layout">
    <el-aside width="232px" class="aside">
      <div class="logo">
        <span class="logo-mark">浙</span>
        <div>
          <strong>餐饮信息</strong>
          <small>运营控制台</small>
        </div>
      </div>
      <el-menu :default-active="activeMenu" router class="menu">
        <el-menu-item index="/">工作台</el-menu-item>
        <el-menu-item index="/audit">审核队列</el-menu-item>
        <el-menu-item index="/posts">信息检索</el-menu-item>
        <el-menu-item index="/regions">地区管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ displayName || '管理端' }}</span>
        <el-button link type="danger" @click="logout">退出登录</el-button>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)
const displayName = computed(() => userStore.displayName || userStore.username)

function logout() {
  userStore.clear()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: #f7f2ea;
}

.aside {
  background: #1c1713;
  color: #f6f0e8;
  border-right: none;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 18px;
  border-bottom: 1px solid rgba(255, 214, 170, 0.12);
}

.logo-mark {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #c87941, #a85a24);
  font-family: 'Noto Serif SC', serif;
  font-weight: 700;
}

.logo strong {
  display: block;
  font-size: 15px;
}

.logo small {
  color: rgba(246, 240, 232, 0.55);
  font-size: 11px;
}

.menu {
  border-right: none;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  color: rgba(246, 240, 232, 0.78);
}

.menu :deep(.el-menu-item.is-active) {
  color: #ffd6aa;
  background: rgba(200, 121, 65, 0.18);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 252, 247, 0.92);
  border-bottom: 1px solid #ece3d8;
}

.main {
  padding: 28px;
}
</style>
