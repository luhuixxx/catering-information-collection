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

      <button class="primary" :loading="saving" @click="saveDraft">保存草稿</button>
      <button class="ghost" :disabled="!lastPostId" :loading="submitting" @click="submit">
        提交审核（需先保存草稿）
      </button>

      <text v-if="lastPostId" class="hint">当前草稿ID：{{ lastPostId }}</text>
      <text v-if="error" class="error">{{ error }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { isLoggedIn } from "@/api/request";
import { saveRecruitDraft, saveTransferDraft, submitPost } from "@/api/post";
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
const lastPostId = ref<number | null>(null);

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
      images: [],
      expireDays: 15,
    };
    if (type.value === "RECRUIT") {
      const res = await saveRecruitDraft({
        ...base,
        jobRole: recruit.value.jobRole,
        salaryType: recruit.value.salaryType,
      });
      lastPostId.value = res.data.postId;
    } else {
      const res = await saveTransferDraft({
        ...base,
        shopCategory: transfer.value.shopCategory,
        areaSqm: transfer.value.areaSqm,
      });
      lastPostId.value = res.data.postId;
    }
    uni.showToast({ title: "草稿已保存", icon: "success" });
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
</style>

