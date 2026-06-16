<template>
  <div class="post-page">
    <div class="page-head">
      <div>
        <h2>信息检索</h2>
        <p>运营视角查看全量信息，支持按编号、标题、电话、城市和状态筛选。</p>
      </div>
      <el-button :loading="loading" @click="load">刷新</el-button>
    </div>

    <el-card shadow="never" class="filters">
      <el-select v-model="query.postType" placeholder="全部类型" clearable>
        <el-option label="招聘" value="RECRUIT" />
        <el-option label="转让" value="TRANSFER" />
        <el-option label="出租" value="RENT" />
        <el-option label="求职" value="JOB_SEEK" />
        <el-option label="招商加盟" value="FRANCHISE" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable>
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已驳回" value="REJECTED" />
        <el-option label="已过期" value="EXPIRED" />
        <el-option label="已下架" value="OFFLINE" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="编号 / 标题 / 说明" clearable />
      <el-input v-model="query.phone" placeholder="联系电话" clearable />
      <el-button type="primary" @click="load">查询</el-button>
    </el-card>

    <el-card shadow="never" class="card">
      <el-table v-loading="loading" :data="rows">
        <el-table-column prop="postNo" label="编号" width="160" />
        <el-table-column prop="postType" label="类型" width="100" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="summary" label="摘要" min-width="260" show-overflow-tooltip />
        <el-table-column label="置顶" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="warning">置顶</el-tag>
            <span v-else class="muted">否</span>
          </template>
        </el-table-column>
        <el-table-column label="地区" width="150">
          <template #default="{ row }">{{ row.cityName }} / {{ row.districtName }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">查看</el-button>
            <el-button v-if="row.isTop !== 1 && row.status === 'APPROVED'" link type="warning" @click="topPost(row.id)">置顶</el-button>
            <el-button v-else-if="row.isTop === 1" link type="info" @click="cancelTop(row.id)">取消</el-button>
            <span v-else class="muted">不可置顶</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <span>共 {{ total }} 条</span>
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="信息详情" width="760px" destroy-on-close>
      <div v-if="detail" class="detail">
        <div class="kv">
          <span>标题</span><b>{{ detail.title }}</b>
          <span>编号</span><b>{{ detail.postNo }}</b>
          <span>联系人</span><b>{{ detail.contactName }}</b>
          <span>电话</span><b>{{ detail.contactPhone }}</b>
          <span>地区</span><b>{{ detail.cityName }} / {{ detail.districtName }}</b>
          <span>状态</span><b>{{ detail.status }}</b>
        </div>
        <p>{{ detail.description || '无补充说明' }}</p>
        <pre>{{ JSON.stringify(detail.ext || {}, null, 2) }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  cancelTopManagedPost,
  fetchManagedPostDetail,
  fetchManagedPosts,
  topManagedPost,
  type ManagedPostItem,
} from '@/api/post'

const loading = ref(false)
const rows = ref<ManagedPostItem[]>([])
const total = ref(0)
const detailVisible = ref(false)
const detail = ref<Record<string, any> | null>(null)
const query = ref({
  postType: '',
  status: '',
  keyword: '',
  phone: '',
})

async function load() {
  loading.value = true
  try {
    const res = await fetchManagedPosts({ ...query.value, page: 1, size: 20 })
    rows.value = res.data.data.records
    total.value = res.data.data.total
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function openDetail(postId: string) {
  detailVisible.value = true
  detail.value = null
  try {
    const res = await fetchManagedPostDetail(postId)
    detail.value = res.data.data as Record<string, any>
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载详情失败')
  }
}

async function topPost(postId: string) {
  try {
    await topManagedPost(postId, 7)
    ElMessage.success('已置顶 7 天')
    await load()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '置顶失败')
  }
}

async function cancelTop(postId: string) {
  try {
    await cancelTopManagedPost(postId)
    ElMessage.success('已取消置顶')
    await load()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '取消失败')
  }
}

load()
</script>

<style scoped>
.post-page {
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

.page-head p {
  margin: 8px 0 0;
  color: #7a6f63;
}

.filters {
  border-radius: 12px;
}

.filters :deep(.el-card__body) {
  display: grid;
  grid-template-columns: 150px 150px minmax(180px, 1fr) 180px 88px;
  gap: 12px;
}

.card {
  border-radius: 12px;
}

.pager {
  padding-top: 14px;
  color: #8b7f73;
  font-size: 13px;
}

.muted {
  color: #9a8f82;
}

.kv {
  display: grid;
  grid-template-columns: 70px 1fr 70px 1fr;
  gap: 10px 12px;
}

.kv span {
  color: #8b7f73;
}

.kv b {
  color: #2a2118;
}

.detail pre {
  padding: 14px;
  border-radius: 10px;
  color: #f6f0e8;
  background: #13110f;
}
</style>
