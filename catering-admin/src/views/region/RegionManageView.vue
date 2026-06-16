<template>
  <div class="region-page">
    <div class="page-head">
      <div>
        <h2>地区管理</h2>
        <p>维护浙江省下的市、区县数据，供用户端筛选与发布使用</p>
      </div>
      <el-button type="primary" @click="openCreate">新增地区</el-button>
    </div>

    <el-card shadow="never" class="tree-card">
      <el-tree
        v-loading="loading"
        :data="treeData"
        node-key="id"
        default-expand-all
        :props="{ label: 'name', children: 'children' }"
      >
        <template #default="{ data }">
          <div class="tree-row">
            <span class="name">{{ data.name }}</span>
            <el-tag size="small" type="info">{{ levelLabel(data.level) }}</el-tag>
            <el-tag size="small" :type="data.enabled ? 'success' : 'info'">
              {{ data.enabled ? '启用' : '停用' }}
            </el-tag>
            <span class="meta">排序 {{ data.sortNo }}</span>
            <div class="actions">
              <el-button link type="primary" @click.stop="openEdit(data)">编辑</el-button>
              <el-button link @click.stop="toggleEnabled(data)">
                {{ data.enabled ? '停用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click.stop="removeRegion(data)">删除</el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="父级 ID" prop="parentId">
          <el-input-number v-model="form.parentId" :min="0" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="层级" prop="level">
          <el-select v-model="form.level" style="width: 100%">
            <el-option label="省" :value="1" />
            <el-option label="市" :value="2" />
            <el-option label="区县" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：杭州市" />
        </el-form-item>
        <el-form-item label="区划代码">
          <el-input v-model="form.code" placeholder="可选，如 330100" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortNo" :min="0" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createRegion,
  deleteRegion,
  fetchRegionTree,
  setRegionEnabled,
  updateRegion,
  type RegionNode,
} from '@/api/region'

const loading = ref(false)
const saving = ref(false)
const treeData = ref<RegionNode[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增地区')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  parentId: 330000,
  level: 2,
  code: '',
  name: '',
  sortNo: 0,
  enabled: 1,
})

const rules: FormRules = {
  parentId: [{ required: true, message: '请输入父级 ID', trigger: 'blur' }],
  level: [{ required: true, message: '请选择层级', trigger: 'change' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
}

function levelLabel(level: number) {
  return level === 1 ? '省' : level === 2 ? '市' : '区县'
}

async function loadTree() {
  loading.value = true
  try {
    const res = await fetchRegionTree()
    treeData.value = res.data.data
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.parentId = 330000
  form.level = 2
  form.code = ''
  form.name = ''
  form.sortNo = 0
  form.enabled = 1
}

function openCreate() {
  editingId.value = null
  dialogTitle.value = '新增地区'
  resetForm()
  dialogVisible.value = true
}

function openEdit(node: RegionNode) {
  editingId.value = node.id
  dialogTitle.value = '编辑地区'
  form.parentId = node.parentId
  form.level = node.level
  form.code = node.code || ''
  form.name = node.name
  form.sortNo = node.sortNo
  form.enabled = node.enabled
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = {
      parentId: form.parentId,
      level: form.level,
      code: form.code || undefined,
      name: form.name,
      sortNo: form.sortNo,
      enabled: form.enabled,
    }
    if (editingId.value) {
      await updateRegion(editingId.value, payload)
      ElMessage.success('已更新')
    } else {
      await createRegion(payload)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    await loadTree()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleEnabled(node: RegionNode) {
  try {
    await setRegionEnabled(node.id, !node.enabled)
    ElMessage.success(node.enabled ? '已停用' : '已启用')
    await loadTree()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function removeRegion(node: RegionNode) {
  try {
    await ElMessageBox.confirm(`确定删除「${node.name}」？`, '删除确认', { type: 'warning' })
    await deleteRegion(node.id)
    ElMessage.success('已删除')
    await loadTree()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e instanceof Error ? e.message : '删除失败')
    }
  }
}

onMounted(loadTree)
</script>

<style scoped>
.region-page {
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

.tree-card {
  border: 1px solid #ece3d8;
  border-radius: 12px;
}

.tree-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding-right: 12px;
}

.name {
  font-weight: 500;
  min-width: 120px;
}

.meta {
  color: #9a8f82;
  font-size: 12px;
}

.actions {
  margin-left: auto;
}
</style>
