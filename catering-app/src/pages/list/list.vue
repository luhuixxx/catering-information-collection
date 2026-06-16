<template>
  <view class="page">
    <view class="topbar">
      <text class="title">{{ typeLabel }}</text>
      <text class="count">{{ total }} 条有效信息</text>
    </view>

    <RegionPicker v-model="region" />

    <view class="filters">
      <input v-model="keyword" class="search" placeholder="搜标题、说明、岗位或经营类型" confirm-type="search" @confirm="reload" />
      <view class="filter-row">
        <input v-if="showRoleFilter" v-model="jobRole" class="filter-input" :placeholder="rolePlaceholder" />
        <input v-if="showCategoryFilter" v-model="shopCategory" class="filter-input" :placeholder="categoryPlaceholder" />
      </view>
      <view v-if="showAmountFilter" class="filter-row">
        <input v-model.number="minSalary" class="filter-input" type="number" :placeholder="amountMinPlaceholder" />
        <input v-model.number="maxSalary" class="filter-input" type="number" :placeholder="amountMaxPlaceholder" />
      </view>
      <view v-if="postType === 'RENT'" class="chips">
        <button :class="['chip', canCatering === true ? 'active' : '']" @click="toggleCatering">可餐饮</button>
        <button :class="['chip', canOpenFlame === true ? 'active' : '']" @click="toggleOpenFlame">可明火</button>
      </view>
      <button class="primary" :loading="loading" @click="reload">筛选</button>
    </view>

    <view class="list">
      <view v-if="loading" class="state">加载中...</view>
      <view v-else-if="records.length === 0" class="state">暂无符合条件的信息</view>
      <view v-for="item in records" v-else :key="item.id" class="post-card" @click="goDetail(item.id)">
        <image v-if="item.coverImage" class="cover" :src="item.coverImage" mode="aspectFill" />
        <view class="content">
          <view class="line">
            <text class="badge">{{ labelOf(item.postType) }}</text>
            <text v-if="item.isTop === 1" class="top">置顶</text>
          </view>
          <text class="post-title">{{ item.title }}</text>
          <text class="summary">{{ item.summary }}</text>
          <view class="meta">
            <text>{{ item.cityName }} · {{ item.districtName }}</text>
            <text>{{ dateText(item.createdAt) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import RegionPicker from "@/components/RegionPicker.vue";
import { fetchPublicPosts, type PublicPostItem } from "@/api/post";

const postType = ref("RECRUIT");
const keyword = ref("");
const jobRole = ref("");
const shopCategory = ref("");
const minSalary = ref<number | undefined>();
const maxSalary = ref<number | undefined>();
const canCatering = ref<boolean | undefined>();
const canOpenFlame = ref<boolean | undefined>();
const region = ref<{ cityId?: number; districtId?: number; label?: string }>({});
const loading = ref(false);
const total = ref(0);
const records = ref<PublicPostItem[]>([]);

const typeLabel = computed(() => labelOf(postType.value));
const showRoleFilter = computed(() => ["RECRUIT", "JOB_SEEK", "FRANCHISE"].includes(postType.value));
const showCategoryFilter = computed(() => ["RECRUIT", "TRANSFER", "JOB_SEEK", "FRANCHISE"].includes(postType.value));
const showAmountFilter = computed(() => ["RECRUIT", "JOB_SEEK", "RENT"].includes(postType.value));
const rolePlaceholder = computed(() => postType.value === "FRANCHISE" ? "品牌名" : postType.value === "JOB_SEEK" ? "期望岗位" : "岗位");
const categoryPlaceholder = computed(() => postType.value === "JOB_SEEK" ? "擅长菜系" : postType.value === "FRANCHISE" ? "加盟品类" : postType.value === "RECRUIT" ? "门店类型" : "经营类型");
const amountMinPlaceholder = computed(() => postType.value === "RENT" ? "最低月租" : "最低薪资");
const amountMaxPlaceholder = computed(() => postType.value === "RENT" ? "最高月租" : "最高薪资");

function labelOf(type: string) {
  const map: Record<string, string> = {
    RECRUIT: "招聘信息",
    TRANSFER: "转让信息",
    RENT: "出租信息",
    JOB_SEEK: "求职信息",
    FRANCHISE: "招商加盟",
  };
  return map[type] || "全部信息";
}

function dateText(value: string) {
  return value ? value.replace("T", " ").slice(5, 16) : "";
}

async function load() {
  loading.value = true;
  try {
    const res = await fetchPublicPosts({
      postType: postType.value,
      keyword: keyword.value,
      cityId: region.value.cityId,
      districtId: region.value.districtId,
      jobRole: showRoleFilter.value ? jobRole.value : undefined,
      shopCategory: showCategoryFilter.value ? shopCategory.value : undefined,
      minSalary: showAmountFilter.value ? minSalary.value : undefined,
      maxSalary: showAmountFilter.value ? maxSalary.value : undefined,
      canCatering: postType.value === "RENT" ? canCatering.value : undefined,
      canOpenFlame: postType.value === "RENT" ? canOpenFlame.value : undefined,
      page: 1,
      size: 20,
    });
    records.value = res.data.records;
    total.value = res.data.total;
  } catch (e) {
    records.value = [];
    uni.showToast({ title: e instanceof Error ? e.message : "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function reload() {
  load();
}

function toggleCatering() {
  canCatering.value = canCatering.value === true ? undefined : true;
}

function toggleOpenFlame() {
  canOpenFlame.value = canOpenFlame.value === true ? undefined : true;
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` });
}

onLoad((query) => {
  if (typeof query?.type === "string") {
    postType.value = query.type;
  }
  if (typeof query?.keyword === "string") {
    keyword.value = decodeURIComponent(query.keyword);
  }
  if (typeof query?.cityId === "string") {
    region.value.cityId = Number(query.cityId);
  }
  if (typeof query?.districtId === "string") {
    region.value.districtId = Number(query.districtId);
  }
});

onShow(load);
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 36rpx 28rpx 56rpx;
  background: #f7f2ea;
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20rpx;
}

.title {
  font-size: 42rpx;
  font-weight: 800;
  color: #2a2118;
}

.count {
  color: #9a8f82;
  font-size: 23rpx;
}

.filters {
  margin-top: 18rpx;
  padding: 22rpx;
  border-radius: 18rpx;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.search,
.filter-input {
  height: 78rpx;
  padding: 0 22rpx;
  border-radius: 14rpx;
  background: #faf5ee;
  font-size: 27rpx;
  box-sizing: border-box;
}

.filter-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 14rpx;
}

.filter-row:empty {
  display: none;
}

.chips {
  display: flex;
  gap: 12rpx;
}

.chip {
  flex: 1;
  height: 70rpx;
  line-height: 70rpx;
  border-radius: 14rpx;
  color: #6f6255;
  background: #faf5ee;
  font-size: 26rpx;
}

.chip.active {
  color: #fff;
  background: #2a2118;
}

.primary {
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 16rpx;
  background: #a85a24;
  color: #fff;
  font-size: 29rpx;
}

.list {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.post-card {
  display: flex;
  gap: 18rpx;
  padding: 18rpx;
  border-radius: 18rpx;
  background: #fff;
  box-shadow: 0 12rpx 30rpx rgba(42, 33, 24, 0.05);
}

.cover {
  width: 144rpx;
  height: 144rpx;
  border-radius: 14rpx;
  background: #eadfcf;
  flex-shrink: 0;
}

.content {
  min-width: 0;
  flex: 1;
}

.line {
  display: flex;
  gap: 10rpx;
}

.badge,
.top {
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
  font-size: 21rpx;
}

.badge {
  background: #f1e7da;
  color: #a85a24;
}

.top {
  background: #2a2118;
  color: #fff6ea;
}

.post-title {
  display: block;
  margin-top: 8rpx;
  font-size: 30rpx;
  color: #2a2118;
  font-weight: 700;
}

.summary {
  display: block;
  margin-top: 6rpx;
  color: #66594d;
  font-size: 24rpx;
}

.meta {
  margin-top: 12rpx;
  display: flex;
  justify-content: space-between;
  color: #9a8f82;
  font-size: 22rpx;
}

.state {
  padding: 36rpx;
  text-align: center;
  color: #8f8578;
  font-size: 25rpx;
}
</style>
