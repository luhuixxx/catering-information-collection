<template>
  <view class="page">
    <view class="hero">
      <text class="title">发布信息</text>
      <text class="subtitle">阶段2：先支持 招聘 / 转让，提交后进入人工审核</text>
    </view>

    <view class="tabs">
      <view class="tab" :class="{ active: type === 'RECRUIT' }" @click="type = 'RECRUIT'">招聘</view>
      <view class="tab" :class="{ active: type === 'TRANSFER' }" @click="type = 'TRANSFER'">转让</view>
    </view>

    <RegionPicker v-model="region" />

    <view class="card">
      <view class="token-bar">
        <text class="token-label">上传凭证</text>
        <text class="token-value">{{ uploadTokenText }}</text>
      </view>

      <view class="field">
        <text class="label">标题</text>
        <input v-model="form.title" class="input" placeholder="例如：西湖区某店招聘厨师/收银" />
      </view>

      <view class="field">
        <text class="label">详细地址（可选）</text>
        <input v-model="form.address" class="input" placeholder="街道/商圈/门牌号" />
      </view>

      <view class="field">
        <text class="label">联系人</text>
        <input v-model="form.contactName" class="input" placeholder="姓名" />
      </view>

      <view class="field">
        <text class="label">联系电话</text>
        <input v-model="form.contactPhone" class="input" type="number" placeholder="手机号" />
      </view>

      <view class="field">
        <text class="label">补充说明</text>
        <textarea v-model="form.description" class="textarea" placeholder="可填写店铺情况、要求、优势等" />
      </view>

      <view class="field">
        <text class="label">图片上传（直传 MinIO）</text>
        <view class="img-row">
          <button class="mini-btn" :disabled="uploading || images.length >= 9" @click="chooseAndUploadImage">
            {{ uploading ? "上传中..." : "选择图片" }}
          </button>
          <text class="upload-tip">最多 9 张（点击已上传地址可移除）</text>
        </view>
        <view class="chips">
          <text v-for="(img, idx) in images" :key="img + idx" class="chip" @click="removeImage(idx)">
            {{ idx + 1 }}. {{ shortUrl(img) }}
          </text>
        </view>
      </view>

      <view v-if="type === 'RECRUIT'" class="block">
        <view class="field">
          <text class="label">岗位</text>
          <input v-model="recruit.jobRole" class="input" placeholder="如：厨师/切配/收银" />
        </view>
        <view class="field">
          <text class="label">薪资类型</text>
          <picker :range="salaryTypes" @change="onSalaryChange">
            <view class="picker">{{ recruit.salaryType }}</view>
          </picker>
        </view>
      </view>

      <view v-else class="block">
        <view class="field">
          <text class="label">经营类型</text>
          <input v-model="transfer.shopCategory" class="input" placeholder="如：快餐/烧烤/奶茶" />
        </view>
        <view class="field">
          <text class="label">面积（㎡）</text>
          <input v-model.number="transfer.areaSqm" class="input" type="number" placeholder="例如 60" />
        </view>
      </view>

      <view v-if="currentStatus" class="status-bar">当前状态：{{ currentStatus }}</view>
      <button class="primary" :loading="saving" @click="saveDraft">{{ isEditMode ? "保存修改" : "保存草稿" }}</button>
      <button class="ghost" :disabled="!lastPostId" :loading="submitting" @click="submit">
        提交审核（需先保存草稿）
      </button>

      <text v-if="lastPostId" class="hint">当前草稿ID：{{ lastPostId }}</text>
      <text v-if="error" class="error">{{ error }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { isLoggedIn } from "@/api/request";
import {
  fetchEditablePost,
  fetchUploadToken,
  saveRecruitDraft,
  saveTransferDraft,
  submitPost,
  updateRecruitDraft,
  updateTransferDraft,
  uploadImageToMinio,
} from "@/api/post";
import RegionPicker from "@/components/RegionPicker.vue";

const type = ref<"RECRUIT" | "TRANSFER">("RECRUIT");
const salaryTypes = ["MONTHLY", "NEGOTIABLE"];

const region = ref<{ cityId?: number; districtId?: number; label?: string }>({});
const form = ref({
  title: "",
  address: "",
  contactName: "",
  contactPhone: "",
  description: "",
});

const recruit = ref({
  jobRole: "",
  salaryType: "MONTHLY",
});

const transfer = ref({
  shopCategory: "",
  areaSqm: 60,
});

const saving = ref(false);
const submitting = ref(false);
const error = ref("");
const lastPostId = ref<string | null>(null);
const images = ref<string[]>([]);
const uploadTokenText = ref("获取中...");
const uploading = ref(false);
const isEditMode = ref(false);
const currentStatus = ref("");

function onSalaryChange(e: any) {
  recruit.value.salaryType = salaryTypes[e.detail.value] || "MONTHLY";
}

async function saveDraft() {
  if (!isLoggedIn()) {
    uni.showToast({ title: "请先登录", icon: "none" });
    uni.navigateTo({ url: "/pages/login/login" });
    return;
  }
  if (!region.value.cityId || !region.value.districtId) {
    error.value = "请选择地区";
    return;
  }
  saving.value = true;
  error.value = "";
  try {
    const base = {
      title: form.value.title,
      cityId: region.value.cityId,
      districtId: region.value.districtId,
      address: form.value.address,
      contactName: form.value.contactName,
      contactPhone: form.value.contactPhone,
      description: form.value.description,
      images: images.value,
      expireDays: 15,
    };
    if (type.value === "RECRUIT") {
      const payload = {
        ...base,
        jobRole: recruit.value.jobRole,
        salaryType: recruit.value.salaryType,
      };
      if (isEditMode.value && lastPostId.value) {
        await updateRecruitDraft(lastPostId.value, payload);
      } else {
        const res = await saveRecruitDraft(payload);
        lastPostId.value = res.data.postId;
      }
    } else {
      const payload = {
        ...base,
        shopCategory: transfer.value.shopCategory,
        areaSqm: transfer.value.areaSqm,
      };
      if (isEditMode.value && lastPostId.value) {
        await updateTransferDraft(lastPostId.value, payload);
      } else {
        const res = await saveTransferDraft(payload);
        lastPostId.value = res.data.postId;
      }
    }
    uni.showToast({ title: isEditMode.value ? "修改已保存" : "草稿已保存", icon: "success" });
  } catch (e) {
    error.value = e instanceof Error ? e.message : "保存失败";
  } finally {
    saving.value = false;
  }
}

async function submit() {
  if (!lastPostId.value) return;
  submitting.value = true;
  error.value = "";
  try {
    await submitPost(lastPostId.value);
    uni.showToast({ title: "已提交审核", icon: "success" });
  } catch (e) {
    error.value = e instanceof Error ? e.message : "提交失败";
  } finally {
    submitting.value = false;
  }
}

async function chooseAndUploadImage() {
  if (images.value.length >= 9) {
    uni.showToast({ title: "最多上传9张", icon: "none" });
    return;
  }
  uploading.value = true;
  try {
    const chooseRes = await new Promise<UniApp.ChooseImageSuccessCallbackResult>((resolve, reject) => {
      uni.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: resolve,
        fail: reject,
      });
    });
    const filePath = chooseRes.tempFilePaths[0];
    if (!filePath) {
      uploading.value = false;
      return;
    }
    const objectUrl = await uploadImageToMinio(filePath);
    images.value = [...images.value, objectUrl].slice(0, 9);
    uni.showToast({ title: "图片已上传", icon: "success" });
  } catch (e) {
    error.value = e instanceof Error ? e.message : "图片上传失败";
  } finally {
    uploading.value = false;
  }
}

function removeImage(idx: number) {
  images.value = images.value.filter((_, i) => i !== idx);
}

function shortUrl(url: string) {
  return url.length > 30 ? `${url.slice(0, 30)}...` : url;
}

onMounted(async () => {
  try {
    const res = await fetchUploadToken("bootstrap.jpg", "image/jpeg");
    uploadTokenText.value = `${res.data.provider}（${new Date(res.data.expireAt).toLocaleTimeString()} 到期）`;
  } catch {
    uploadTokenText.value = "获取失败";
  }
});

onLoad(async (query) => {
  const postId = typeof query?.postId === "string" ? query.postId : "";
  if (!postId) return;
  isEditMode.value = true;
  lastPostId.value = postId;
  try {
    const res = await fetchEditablePost(postId);
    const data = res.data as any;
    const post = data.post || {};
    const ext = data.ext || {};
    const imgList = (data.images || []) as Array<{ url?: string }>;
    type.value = post.postType === "TRANSFER" ? "TRANSFER" : "RECRUIT";
    currentStatus.value = post.status || "";
    form.value.title = post.title || "";
    form.value.address = post.address || "";
    form.value.contactName = post.contactName || "";
    form.value.contactPhone = post.contactPhone || "";
    form.value.description = post.description || "";
    region.value.cityId = post.cityId;
    region.value.districtId = post.districtId;
    images.value = imgList.map((i) => i.url || "").filter(Boolean);
    if (type.value === "RECRUIT") {
      recruit.value.jobRole = ext.jobRole || "";
      recruit.value.salaryType = ext.salaryType || "MONTHLY";
    } else {
      transfer.value.shopCategory = ext.shopCategory || "";
      transfer.value.areaSqm = ext.areaSqm || 60;
    }
  } catch (e) {
    uni.showToast({ title: e instanceof Error ? e.message : "加载编辑数据失败", icon: "none" });
  }
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 48rpx 32rpx 64rpx;
  background: linear-gradient(180deg, #fff6ea 0%, #f7f2ea 100%);
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}

.hero .title {
  font-size: 44rpx;
  font-weight: 800;
  color: #2a2118;
}

.hero .subtitle {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #7a6f63;
}

.tabs {
  display: flex;
  gap: 14rpx;
}

.tab {
  padding: 14rpx 22rpx;
  border-radius: 999rpx;
  background: #f1e7da;
  color: #7a6f63;
  font-size: 26rpx;
}
.tab.active {
  background: #c87941;
  color: #fff;
}

.card {
  padding: 28rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 16rpx 48rpx rgba(42, 33, 24, 0.08);
}

.token-bar {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 22rpx;
  padding: 16rpx 18rpx;
  border-radius: 14rpx;
  background: rgba(200, 121, 65, 0.12);
}

.token-label {
  color: #7a6f63;
  font-size: 22rpx;
}

.token-value {
  color: #2a2118;
  font-size: 22rpx;
}

.field {
  margin-bottom: 22rpx;
}
.label {
  display: block;
  margin-bottom: 10rpx;
  font-size: 24rpx;
  color: #9a8f82;
}
.input {
  height: 84rpx;
  padding: 0 24rpx;
  border-radius: 16rpx;
  background: #faf5ee;
  font-size: 30rpx;
}
.textarea {
  width: 100%;
  min-height: 160rpx;
  padding: 18rpx 22rpx;
  border-radius: 16rpx;
  background: #faf5ee;
  font-size: 28rpx;
}

.img-row {
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.mini-btn {
  min-width: 160rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 14rpx;
  background: #f1e7da;
  color: #a85a24;
  font-size: 24rpx;
}

.upload-tip {
  color: #9a8f82;
  font-size: 22rpx;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 12rpx;
}

.chip {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #f8ede1;
  color: #7a6f63;
  font-size: 22rpx;
}

.picker {
  height: 84rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  border-radius: 16rpx;
  background: #faf5ee;
  font-size: 28rpx;
  color: #2a2118;
}

.primary,
.ghost {
  height: 92rpx;
  line-height: 92rpx;
  border-radius: 18rpx;
  font-size: 30rpx;
}
.primary {
  background: linear-gradient(135deg, #c87941, #a85a24);
  color: #fff;
  margin-top: 10rpx;
}
.ghost {
  margin-top: 14rpx;
  background: #f1e7da;
  color: #a85a24;
}
.hint {
  display: block;
  margin-top: 14rpx;
  color: #9a8f82;
  font-size: 24rpx;
}
.error {
  display: block;
  margin-top: 12rpx;
  color: #d64545;
  font-size: 24rpx;
}

.status-bar {
  margin-bottom: 12rpx;
  padding: 12rpx 16rpx;
  border-radius: 12rpx;
  font-size: 24rpx;
  color: #7a6f63;
  background: #f5ecdf;
}
</style>

