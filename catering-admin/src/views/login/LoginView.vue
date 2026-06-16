<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="brand">
        <p class="brand-tag">浙江省 · 餐饮信息运营台</p>
        <h1>信息采集管理端</h1>
        <p class="brand-desc">审核发布、地区维护、内容治理的统一入口</p>
      </div>

      <el-form class="login-form" @submit.prevent="handleLogin">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入管理员账号" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">
          进入工作台
        </el-button>
        <p v-if="error" class="error">{{ error }}</p>
        <p class="hint">开发环境默认账号：admin / admin123</p>
      </el-form>
    </div>
    <div class="login-visual" aria-hidden="true">
      <div class="orb orb-a"></div>
      <div class="orb orb-b"></div>
      <div class="grid"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const error = ref('')
const form = reactive({ username: 'admin', password: '' })

async function handleLogin() {
  if (!form.username || !form.password) {
    error.value = '请输入账号和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await adminLogin(form.username, form.password)
    const data = res.data.data
    userStore.setSession({
      token: data.token,
      username: data.username,
      displayName: data.displayName,
    })
    ElMessage.success(`欢迎回来，${data.displayName || data.username}`)
    router.push('/')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(360px, 480px) 1fr;
  background: #14110f;
  color: #f6f0e8;
  font-family: 'Noto Sans SC', sans-serif;
}

.login-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 56px;
  background: linear-gradient(165deg, #1c1713 0%, #12100e 100%);
  border-right: 1px solid rgba(255, 214, 170, 0.12);
  position: relative;
  z-index: 2;
}

.brand-tag {
  margin: 0 0 12px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #d4a574;
}

.brand h1 {
  margin: 0;
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  font-weight: 600;
  line-height: 1.25;
}

.brand-desc {
  margin: 14px 0 36px;
  color: rgba(246, 240, 232, 0.72);
  line-height: 1.6;
}

.login-form :deep(.el-form-item__label) {
  color: rgba(246, 240, 232, 0.85);
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.04);
  box-shadow: 0 0 0 1px rgba(255, 214, 170, 0.18) inset;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
  height: 44px;
  font-size: 15px;
  letter-spacing: 0.08em;
  background: linear-gradient(135deg, #c87941, #a85a24);
  border: none;
}

.error {
  margin: 12px 0 0;
  color: #ff8a80;
  font-size: 13px;
}

.hint {
  margin: 16px 0 0;
  font-size: 12px;
  color: rgba(246, 240, 232, 0.45);
}

.login-visual {
  position: relative;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.55;
}

.orb-a {
  width: 420px;
  height: 420px;
  background: #c87941;
  top: -80px;
  right: -60px;
}

.orb-b {
  width: 360px;
  height: 360px;
  background: #3d6b5a;
  bottom: -100px;
  left: 10%;
}

.grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(circle at 30% 40%, black, transparent 70%);
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-visual {
    display: none;
  }
}
</style>
