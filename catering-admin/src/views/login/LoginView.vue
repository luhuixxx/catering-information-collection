<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>管理端登录</template>
      <el-form @submit.prevent>
        <el-form-item label="账号">
          <el-input placeholder="待接入" disabled />
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" placeholder="待接入" disabled />
        </el-form-item>
        <el-button type="primary" disabled>登录（待实现）</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.login-card {
  width: 400px;
}
</style>
