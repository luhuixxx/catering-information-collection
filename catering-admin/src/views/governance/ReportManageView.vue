<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h2>举报处理</h2>
        <p>集中处理用户举报，支持忽略、下架信息和封禁发布者。</p>
      </div>
      <el-button :loading="loading" @click="load">刷新</el-button>
    </div>

    <el-card shadow="never" class="filters">
      <el-select v-model="query.status" placeholder="全部状态" clearable>
        <el-option label="待处理" value="PENDING" />
        <el-option label="已处理" value="DONE" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </el-card>

    <el-card shadow="never" class="card">
      <el-table v-loading="loading" :data="rows">
        <el-table-column label="举报信息" min-width="260">
          <template #default="{ row }">
            <b>{{ row.postTitle }}</b>
            <small>{{ row.postNo }} · {{ reasonLabel(row.reason) }}</small>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="举报人" width="150">
          <template #default="{ row }">{{ row.reporterPhone || row.reporterUserId }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'">{{ row.status === 'PENDING' ? '待处理' : '已处理' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button link type="info" @click="openHandle(row, 'IGNORE')">忽略</el-button>
              <el-button link type="warning" @click="openHandle(row, 'OFFLINE')">下架</el-button>
              <el-button link type="danger" @click="openHandle(row, 'BAN')">封禁</el-button>
            </template>
            <span v-else class="muted">{{ actionLabel(row.handledAction) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">共 {{ total }} 条</div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="460px">
      <el-form label-width="88px">
        <el-form-item v-if="handleAction === 'BAN'" label="封禁天数">
          <el-input-number v-model="banDays" :min="1" :max="3650" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="note" type="textarea" :rows="4" placeholder="填写处理依据，便于后续追溯" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchReports, handleReport, type ReportItem } from '@/api/post'

const loading = ref(false)
const submitting = ref(false)
const rows = ref<ReportItem[]>([])
const total = ref(0)
const query = ref({ status: 'PENDING' })
const dialogVisible = ref(false)
const current = ref<ReportItem | null>(null)
const handleAction = ref<'IGNORE' | 'OFFLINE' | 'BAN'>('IGNORE')
const note = ref('')
const banDays = ref(7)
const dialogTitle = computed(() => `${actionLabel(handleAction.value)}：${current.value?.postTitle || ''}`)

function reasonLabel(value: string) {
  return ({ FAKE: '虚假信息', SCAM: '诈骗风险', SPAM: '重复发布', ABUSE: '辱骂骚扰', OTHER: '其他' } as Record<string, string>)[value] || value
}

function actionLabel(value: string) {
  return ({ IGNORE: '忽略', OFFLINE: '下架信息', BAN: '封禁发布者' } as Record<string, string>)[value] || value
}

async function load() {
  loading.value = true
  try {
    const res = await fetchReports({ ...query.value, page: 1, size: 20 })
    rows.value = res.data.data.records
    total.value = res.data.data.total
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function openHandle(row: ReportItem, action: 'IGNORE' | 'OFFLINE' | 'BAN') {
  current.value = row
  handleAction.value = action
  note.value = ''
  banDays.value = 7
  dialogVisible.value = true
}

async function submitHandle() {
  if (!current.value) return
  submitting.value = true
  try {
    await handleReport(current.value.id, handleAction.value, note.value, banDays.value)
    ElMessage.success('已处理')
    dialogVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '处理失败')
  } finally {
    submitting.value = false
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
.muted,
.pager,
small {
  color: #7a6f63;
}

.filters,
.card {
  border-radius: 12px;
}

.filters :deep(.el-card__body) {
  display: flex;
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
