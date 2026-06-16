<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h2>用户治理</h2>
        <p>查看用户发布与举报记录，对违规账号执行封禁或解除封禁。</p>
      </div>
      <el-button :loading="loading" @click="load">刷新</el-button>
    </div>

    <el-card shadow="never" class="filters">
      <el-input v-model="keyword" placeholder="手机号 / 昵称" clearable />
      <el-button type="primary" @click="load">查询</el-button>
    </el-card>

    <el-card shadow="never" class="card">
      <el-table v-loading="loading" :data="rows">
        <el-table-column label="用户" min-width="220">
          <template #default="{ row }">
            <b>{{ row.nickname || '未命名用户' }}</b>
            <small>{{ row.phone || row.id }}</small>
          </template>
        </el-table-column>
        <el-table-column label="内容" width="160">
          <template #default="{ row }">发布 {{ row.postCount }} · 举报 {{ row.reportCount }}</template>
        </el-table-column>
        <el-table-column label="违规" width="90">
          <template #default="{ row }">{{ row.violationCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="150">
          <template #default="{ row }">
            <el-tag :type="row.banned ? 'danger' : 'success'">{{ row.banned ? '封禁中' : '正常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="banReason" label="封禁原因" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.banned" link type="danger" @click="openBan(row)">封禁</el-button>
            <el-button v-else link type="primary" @click="unban(row.id)">解封</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">共 {{ total }} 个用户</div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="封禁用户" width="460px">
      <el-form label-width="96px">
        <el-form-item label="封禁天数">
          <el-input-number v-model="banDays" :min="1" :max="3650" />
        </el-form-item>
        <el-form-item label="同步下架">
          <el-switch v-model="offlinePosts" active-text="下架已通过信息" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="reason" type="textarea" :rows="4" placeholder="例如：举报属实，存在虚假联系方式" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitBan">确认封禁</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { banAppUser, fetchAppUsers, unbanAppUser, type AppUserItem } from '@/api/post'

const loading = ref(false)
const submitting = ref(false)
const rows = ref<AppUserItem[]>([])
const total = ref(0)
const keyword = ref('')
const dialogVisible = ref(false)
const current = ref<AppUserItem | null>(null)
const banDays = ref(7)
const reason = ref('')
const offlinePosts = ref(true)

async function load() {
  loading.value = true
  try {
    const res = await fetchAppUsers({ keyword: keyword.value, page: 1, size: 20 })
    rows.value = res.data.data.records
    total.value = res.data.data.total
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function openBan(row: AppUserItem) {
  current.value = row
  banDays.value = 7
  reason.value = ''
  offlinePosts.value = true
  dialogVisible.value = true
}

async function submitBan() {
  if (!current.value) return
  submitting.value = true
  try {
    await banAppUser(current.value.id, { banDays: banDays.value, reason: reason.value, offlinePosts: offlinePosts.value })
    ElMessage.success('已封禁')
    dialogVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '封禁失败')
  } finally {
    submitting.value = false
  }
}

async function unban(userId: string) {
  try {
    await unbanAppUser(userId)
    ElMessage.success('已解封')
    await load()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '解封失败')
  }
}

load()
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.page-head h2 {
  margin: 0;
  color: #2a2118;
}

.page-head p,
.pager,
small {
  color: #7a6f63;
}

.filters,
.card {
  border-radius: 12px;
}

.filters :deep(.el-card__body) {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 88px;
  gap: 12px;
}

b,
small {
  display: block;
}

b {
  color: #2a2118;
}

small {
  margin-top: 4px;
  font-size: 12px;
}

.pager {
  padding-top: 14px;
  font-size: 13px;
}
</style>
