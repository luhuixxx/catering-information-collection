<template>
  <view class="page">
    <view v-if="loading" class="state">加载中...</view>
    <template v-else-if="detail">
      <swiper v-if="images.length" class="gallery" indicator-dots>
        <swiper-item v-for="img in images" :key="img">
          <image class="gallery-img" :src="img" mode="aspectFill" />
        </swiper-item>
      </swiper>

      <view class="main">
        <view class="headline">
          <text class="badge">{{ labelOf(detail.postType) }}</text>
          <text class="title">{{ detail.title }}</text>
          <text class="meta">{{ detail.cityName }} · {{ detail.districtName }} · 编号 {{ detail.postNo }}</text>
        </view>

        <view class="section">
          <text class="section-title">关键信息</text>
          <view class="kv">
            <text v-for="item in extRows" :key="item.label" class="kv-item">
              <text class="kv-label">{{ item.label }}</text>
              <text class="kv-value">{{ item.value }}</text>
            </text>
          </view>
        </view>

        <view class="section">
          <text class="section-title">补充说明</text>
          <text class="desc">{{ detail.description || "发布者暂未填写补充说明" }}</text>
          <text v-if="detail.address" class="address">地址：{{ detail.address }}</text>
        </view>

        <view class="contact">
          <view>
            <text class="contact-label">联系电话</text>
            <text class="phone">{{ detail.phoneVisible ? detail.contactPhone : detail.contactPhoneMasked }}</text>
            <text class="notice">{{ detail.phoneNotice }}</text>
          </view>
          <button class="call" @click="handleCall">{{ detail.phoneVisible ? "拨打" : "登录查看" }}</button>
        </view>
      </view>
    </template>
    <view v-else class="state">信息不存在或已失效</view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { fetchPublicPostDetail, type PublicPostDetail } from "@/api/post";

const postId = ref("");
const loading = ref(false);
const detail = ref<PublicPostDetail | null>(null);

const images = computed(() => detail.value?.images?.length ? detail.value.images : detail.value?.coverImage ? [detail.value.coverImage] : []);
const extRows = computed(() => {
  const d = detail.value;
  if (!d) return [];
  const ext = d.ext || {};
  if (d.postType === "RECRUIT") {
    return [
      { label: "岗位", value: String(ext.jobRoleOther || ext.jobRole || "-") },
      { label: "薪资", value: salaryText(ext) },
      { label: "门店类型", value: String(ext.shopCategory || "-") },
      { label: "招聘人数", value: `${ext.headcount || 1} 人` },
      { label: "包吃住", value: ext.provideBoard ? "包吃住" : "不含吃住" },
      { label: "经验", value: String(ext.expRequirement || "不限") },
    ];
  }
  if (d.postType === "TRANSFER") {
    return [
      { label: "经营类型", value: String(ext.shopCategory || "-") },
      { label: "面积", value: `${ext.areaSqm || "-"}㎡` },
      { label: "月租", value: ext.rentNegotiable ? "面议" : `${ext.rentMonthly || "-"} 元/月` },
      { label: "转让费", value: ext.feeNegotiable ? "面议" : `${ext.transferFee || "-"} 元` },
      { label: "设备", value: ext.includeEquipment ? "带设备" : "空转" },
      { label: "状态", value: ext.operating ? "营业中" : "已停业" },
    ];
  }
  if (d.postType === "RENT") {
    return [
      { label: "面积", value: `${ext.areaSqm || "-"}㎡` },
      { label: "月租", value: ext.rentNegotiable ? "面议" : `${ext.rentMonthly || "-"} 元/月` },
      { label: "入场费", value: ext.entryFeeNegotiable ? "面议" : `${ext.entryFee || "无"} 元` },
      { label: "餐饮", value: ext.canCatering ? "可餐饮" : "不可餐饮" },
      { label: "明火", value: ext.canOpenFlame ? "可明火" : "不可明火" },
      { label: "楼层", value: String(ext.floorDesc || "-") },
    ];
  }
  if (d.postType === "JOB_SEEK") {
    return [
      { label: "期望岗位", value: String(ext.desiredRoles || "-") },
      { label: "期望薪资", value: salaryText(ext) },
      { label: "工作年限", value: ext.workYears ? `${ext.workYears} 年` : "未填写" },
      { label: "擅长", value: String(ext.cuisines || "-") },
      { label: "年龄", value: ext.age ? `${ext.age} 岁` : "未填写" },
      { label: "性别", value: String(ext.gender || "未填写") },
    ];
  }
  if (d.postType === "FRANCHISE") {
    return [
      { label: "品牌", value: String(ext.brandName || "-") },
      { label: "品类", value: String(ext.category || "-") },
      { label: "投资", value: String(ext.investmentDesc || "面议") },
      { label: "说明", value: String(ext.franchiseDesc || "-") },
    ];
  }
  return [{ label: "类型", value: labelOf(d.postType) }];
});

function labelOf(type: string) {
  const map: Record<string, string> = {
    RECRUIT: "招聘",
    TRANSFER: "转让",
    RENT: "出租",
    JOB_SEEK: "求职",
    FRANCHISE: "招商加盟",
  };
  return map[type] || type;
}

function salaryText(ext: Record<string, unknown>) {
  if (ext.salaryType === "NEGOTIABLE") return "面议";
  if (!ext.salaryMin) return "-";
  return ext.salaryMax ? `${ext.salaryMin}-${ext.salaryMax} 元/月` : `${ext.salaryMin} 元/月起`;
}

async function load() {
  if (!postId.value) return;
  loading.value = true;
  try {
    const res = await fetchPublicPostDetail(postId.value);
    detail.value = res.data;
  } catch {
    detail.value = null;
  } finally {
    loading.value = false;
  }
}

function handleCall() {
  if (!detail.value) return;
  if (!detail.value.phoneVisible) {
    uni.navigateTo({ url: `/pages/login/login?redirect=${encodeURIComponent(`/pages/detail/detail?id=${postId.value}`)}` });
    return;
  }
  uni.makePhoneCall({ phoneNumber: detail.value.contactPhone });
}

onLoad((query) => {
  postId.value = typeof query?.id === "string" ? query.id : "";
});

onShow(load);
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f7f2ea;
}

.gallery {
  height: 420rpx;
  background: #eadfcf;
}

.gallery-img {
  width: 100%;
  height: 420rpx;
}

.main {
  padding: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.headline,
.section,
.contact {
  padding: 24rpx;
  border-radius: 18rpx;
  background: #fff;
}

.badge {
  display: inline-flex;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #f1e7da;
  color: #a85a24;
  font-size: 23rpx;
}

.title {
  display: block;
  margin-top: 12rpx;
  color: #2a2118;
  font-size: 42rpx;
  font-weight: 800;
}

.meta,
.notice,
.kv-label,
.address {
  display: block;
  margin-top: 10rpx;
  color: #9a8f82;
  font-size: 24rpx;
}

.section-title {
  color: #2a2118;
  font-size: 28rpx;
  font-weight: 700;
}

.kv {
  margin-top: 14rpx;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 12rpx;
}

.kv-item {
  padding: 14rpx;
  border-radius: 14rpx;
  background: #faf5ee;
}

.kv-value {
  display: block;
  margin-top: 4rpx;
  color: #2a2118;
  font-size: 27rpx;
  font-weight: 650;
}

.desc {
  display: block;
  margin-top: 14rpx;
  color: #51463a;
  font-size: 28rpx;
  line-height: 1.6;
}

.contact {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20rpx;
  position: sticky;
  bottom: 18rpx;
}

.contact-label {
  color: #9a8f82;
  font-size: 23rpx;
}

.phone {
  display: block;
  margin-top: 4rpx;
  color: #2a2118;
  font-size: 36rpx;
  font-weight: 800;
}

.call {
  min-width: 180rpx;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 16rpx;
  color: #fff;
  background: #a85a24;
  font-size: 28rpx;
}

.state {
  padding: 80rpx 28rpx;
  text-align: center;
  color: #8f8578;
  font-size: 28rpx;
}
</style>
