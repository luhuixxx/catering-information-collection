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

    <el-dialog v-model="detailVisible" title="审核详情" width="760px" destroy-on-close>
      <div v-loading="detailLoading" class="detail">
        <template v-if="detailPost">
          <div class="kv">
            <span>标题</span><b>{{ detailPost.title }}</b>
            <span>类型</span><b>{{ detailPost.postType }}</b>
            <span>状态</span><b>{{ detailPost.status }}</b>
            <span>联系人</span><b>{{ detailPost.contactName }}</b>
            <span>电话</span><b>{{ detailPost.contactPhone }}</b>
            <span>地区</span><b>{{ detailPost.cityId }} / {{ detailPost.districtId }}</b>
            <span>历史驳回</span><b>{{ publisherRejectedCount }} 次</b>
          </div>
          <p class="desc">{{ detailPost.description || '无补充说明' }}</p>
          <div v-if="detailImages.length" class="imgs">
            <img v-for="(img, i) in detailImages" :key="img + i" :src="img" class="img" />
          </div>
          <div class="ext-box">
            <h3>业务字段</h3>
            <pre class="json">{{ detailJson }}</pre>
          </div>
          <div class="history">
            <h3>审核记录</h3>
            <div v-if="auditHistory.length === 0" class="history-empty">暂无历史记录</div>
            <div v-for="item in auditHistory" :key="item.id || item.createdAt" class="history-item">
              <b>{{ item.action }}</b>
              <span>{{ item.reasonText || item.reasonCode || '无说明' }}</span>
              <em>{{ item.createdAt }}</em>
            </div>
          </div>
        </template>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="驳回信息" width="520px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="驳回原因">
          <el-radio-group v-model="rejectForm.reasonCode">
            <el-radio-button v-for="item in rejectReasons" :key="item.value" :label="item.value">
              {{ item.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input
            v-model="rejectForm.reasonText"
            type="textarea"
            :rows="4"
            maxlength="120"
            show-word-limit
            placeholder="请写清楚发布者需要修改什么，例如：请补充真实店面图片，联系电话无法接通。"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="confirmReject">确定驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { auditPost, fetchPendingPostDetail, fetchPendingPosts, type PendingPost } from '@/api/post'

const loading = ref(false)
const postType = ref<string | undefined>()
const rows = ref<PendingPost[]>([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<Record<string, unknown> | null>(null)
const detailJson = computed(() => (detail.value ? JSON.stringify(detail.value, null, 2) : ''))
const detailPost = computed(() => (detail.value?.post || null) as Record<string, any> | null)
const detailImages = computed(() => {
  const arr = (detail.value?.images || []) as Array<{ url?: string }>
  return arr.map((i) => i.url || '').filter(Boolean)
})
const publisherRejectedCount = computed(() => Number(detail.value?.publisherRejectedCount || 0))
const auditHistory = computed(() => (detail.value?.auditHistory || []) as Array<Record<string, any>>)
const rejectVisible = ref(false)
const rejecting = ref(false)
const rejectPostId = ref('')
const rejectForm = ref({
  reasonCode: '',
  reasonText: '',
})

const rejectReasons = [
  { label: '信息不实', value: 'INVALID_INFO' },
  { label: '联系方式无效', value: 'INVALID_CONTACT' },
  { label: '内容违规', value: 'VIOLATION' },
  { label: '图片不合规', value: 'BAD_IMAGE' },
  { label: '重复信息', value: 'DUPLICATE' },
  { label: '其他', value: 'OTHER' },
]

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

async function openDetail(postId: string) {
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

async function approve(postId: string) {
  try {
    await auditPost(postId, 'APPROVE')
    ElMessage.success('已通过')
    await load()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function reject(postId: string) {
  rejectPostId.value = postId
  rejectForm.value = { reasonCode: '', reasonText: '' }
  rejectVisible.value = true
}

async function confirmReject() {
  if (!rejectForm.value.reasonCode) {
    ElMessage.warning('请选择驳回原因')
    return
  }
  if (!rejectForm.value.reasonText.trim()) {
    ElMessage.warning('请填写补充说明')
    return
  }
  rejecting.value = true
  try {
    await auditPost(rejectPostId.value, 'REJECT', rejectForm.value.reasonCode, rejectForm.value.reasonText.trim())
    ElMessage.success('已驳回')
    rejectVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    rejecting.value = false
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
  max-height: 32vh;
  overflow: auto;
}

.kv {
  display: grid;
  grid-template-columns: 70px 1fr 70px 1fr;
  gap: 8px 12px;
  margin-bottom: 10px;
}

.kv span {
  color: #8b7f73;
  font-size: 12px;
}

.kv b {
  color: #2a2118;
  font-size: 13px;
}

.desc {
  margin: 8px 0 12px;
  color: #51463a;
}

.imgs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.img {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee2d4;
}

.ext-box,
.history {
  margin-top: 14px;
}

.ext-box h3,
.history h3 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #2a2118;
}

.history {
  padding: 12px;
  border-radius: 10px;
  background: #fbf6ef;
  border: 1px solid #eee2d4;
}

.history-empty {
  color: #9a8f82;
  font-size: 13px;
}

.history-item {
  display: grid;
  grid-template-columns: 90px 1fr 170px;
  gap: 10px;
  align-items: center;
  padding: 8px 0;
  border-top: 1px solid #eee2d4;
}

.history-item:first-of-type {
  border-top: 0;
}

.history-item b {
  color: #a85a24;
  font-size: 12px;
}

.history-item span {
  color: #51463a;
  font-size: 13px;
}

.history-item em {
  color: #9a8f82;
  font-size: 12px;
  font-style: normal;
}

:deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

:deep(.el-radio-button__inner) {
  border-left: var(--el-border);
  border-radius: 8px !important;
}
</style>
