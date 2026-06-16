<template>
  <div class="audit-page">
    <div class="page-head">
      <div>
        <h2>审核队列</h2>
        <p>待审核信息将显示在此处，支持通过/驳回（阶段2闭环）</p>
      </div>
      <div class="filters">
        <el-select v-model="postType" placeholder="全部类型" clearable style="width: 180px">
          <el-option label="招聘" value="RECRUIT" />
          <el-option label="转让" value="TRANSFER" />
        </el-select>
        <el-button :loading="loading" @click="load">刷新</el-button>
      </div>
    </div>

    <el-card shadow="never" class="card">
      <el-table v-loading="loading" :data="rows" style="width: 100%">
        <el-table-column prop="postNo" label="编号" width="160" />
        <el-table-column prop="postType" label="类型" width="110" />
        <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip />
        <el-table-column prop="publisherUserId" label="发布者" width="120" />
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">查看</el-button>
            <el-button link type="success" @click="approve(row.id)">通过</el-button>
            <el-button link type="danger" @click="reject(row.id)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && rows.length === 0" class="empty">暂无待审数据</div>
    </el-card>

    <el-dialog v-model="detailVisible" title="审核详情" width="720px" destroy-on-close>
      <div v-loading="detailLoading" class="detail">
        <pre class="json">{{ detailJson }}</pre>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditPost, fetchPendingPostDetail, fetchPendingPosts, type PendingPost } from '@/api/post'

const loading = ref(false)
const postType = ref<string | undefined>()
const rows = ref<PendingPost[]>([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<Record<string, unknown> | null>(null)
const detailJson = computed(() => (detail.value ? JSON.stringify(detail.value, null, 2) : ''))

async function load() {
  loading.value = true
  try {
    const res = await fetchPendingPosts(postType.value)
    rows.value = res.data.data
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function openDetail(postId: number) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await fetchPendingPostDetail(postId)
    detail.value = res.data.data
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

async function approve(postId: number) {
  try {
    await auditPost(postId, 'APPROVE')
    ElMessage.success('已通过')
    await load()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function reject(postId: number) {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因（可空）', '驳回', {
      confirmButtonText: '确定驳回',
      cancelButtonText: '取消',
      inputPlaceholder: '如：图片不清晰/联系方式不规范/标题过短等',
    })
    await auditPost(postId, 'REJECT', value)
    ElMessage.success('已驳回')
    await load()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e instanceof Error ? e.message : '操作失败')
    }
  }
}

load()
</script>

<style scoped>
.audit-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-head h2 {
  margin: 0;
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  color: #2a2118;
}

.page-head p {
  margin: 8px 0 0;
  color: #7a6f63;
  font-size: 14px;
}

.filters {
  display: flex;
  gap: 12px;
  align-items: center;
}

.card {
  border: 1px solid #ece3d8;
  border-radius: 12px;
}

.empty {
  padding: 18px;
  color: #9a8f82;
  font-size: 13px;
}

.json {
  margin: 0;
  padding: 14px;
  border-radius: 10px;
  background: #13110f;
  color: #f6f0e8;
  max-height: 56vh;
  overflow: auto;
}
</style>

