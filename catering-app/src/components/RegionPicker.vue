<template>
  <view class="picker-root">
    <view class="picker-trigger" @click="open = true">
      <text class="label">当前地区</text>
      <text class="value">{{ displayText }}</text>
      <text class="arrow">›</text>
    </view>

    <view v-if="open" class="mask" @click="open = false">
      <view class="sheet" @click.stop>
        <view class="sheet-head">
          <text class="sheet-title">选择城市 / 区县</text>
          <text class="close" @click="open = false">关闭</text>
        </view>

        <view class="tabs">
          <view
            v-for="(tab, index) in tabs"
            :key="index"
            class="tab"
            :class="{ active: step === index }"
            @click="step = index"
          >
            {{ tab || placeholder[index] }}
          </view>
        </view>

        <scroll-view scroll-y class="options">
          <view
            v-for="item in currentOptions"
            :key="item.id"
            class="option"
            :class="{ selected: isSelected(item) }"
            @click="selectItem(item)"
          >
            {{ item.name }}
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { fetchRegionChildren, fetchRegionTree, type RegionNode } from "@/api/region";

const props = defineProps<{ modelValue?: { cityId?: number; districtId?: number; label?: string } }>();
const emit = defineEmits<{
  (e: "update:modelValue", value: { cityId: number; districtId: number; label: string }): void;
}>();

const open = ref(false);
const step = ref(0);
const province = ref<RegionNode | null>(null);
const cities = ref<RegionNode[]>([]);
const districts = ref<RegionNode[]>([]);
const selectedCity = ref<RegionNode | null>(null);
const selectedDistrict = ref<RegionNode | null>(null);

const placeholder = ["选择城市", "选择区县"];
const tabs = computed(() => [selectedCity.value?.name || "", selectedDistrict.value?.name || ""]);

const displayText = computed(() => {
  if (props.modelValue?.label) return props.modelValue.label;
  if (selectedCity.value && selectedDistrict.value) {
    return `${selectedCity.value.name} · ${selectedDistrict.value.name}`;
  }
  return "请选择所在地区";
});

const currentOptions = computed(() => (step.value === 0 ? cities.value : districts.value));

function isSelected(item: RegionNode) {
  return step.value === 0 ? selectedCity.value?.id === item.id : selectedDistrict.value?.id === item.id;
}

async function loadTree() {
  const res = await fetchRegionTree();
  province.value = res.data[0] || null;
  if (province.value?.children?.length) {
    cities.value = province.value.children;
    return;
  }
  if (province.value) {
    const childRes = await fetchRegionChildren(province.value.id);
    cities.value = childRes.data;
  }
}

async function selectItem(item: RegionNode) {
  if (step.value === 0) {
    selectedCity.value = item;
    selectedDistrict.value = null;
    const res = await fetchRegionChildren(item.id);
    districts.value = res.data;
    step.value = 1;
    return;
  }

  selectedDistrict.value = item;
  if (selectedCity.value) {
    const label = `${selectedCity.value.name} · ${item.name}`;
    emit("update:modelValue", {
      cityId: selectedCity.value.id,
      districtId: item.id,
      label,
    });
    uni.setStorageSync("selected_region", {
      cityId: selectedCity.value.id,
      districtId: item.id,
      label,
    });
  }
  open.value = false;
}

watch(open, (visible) => {
  if (visible) step.value = selectedDistrict.value ? 1 : 0;
});

onMounted(async () => {
  await loadTree();
  const cached = uni.getStorageSync("selected_region");
  if (cached?.cityId) {
    selectedCity.value = cities.value.find((c) => c.id === cached.cityId) || null;
    if (selectedCity.value) {
      const res = await fetchRegionChildren(selectedCity.value.id);
      districts.value = res.data;
      selectedDistrict.value = districts.value.find((d) => d.id === cached.districtId) || null;
      emit("update:modelValue", cached);
    }
  }
});
</script>

<style scoped>
.picker-root {
  width: 100%;
}

.picker-trigger {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx 28rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 12rpx 40rpx rgba(42, 33, 24, 0.08);
}

.label {
  font-size: 24rpx;
  color: #9a8f82;
}

.value {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: #2a2118;
}

.arrow {
  color: #c87941;
  font-size: 36rpx;
}

.mask {
  position: fixed;
  inset: 0;
  background: rgba(20, 17, 15, 0.45);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.sheet {
  width: 100%;
  max-height: 70vh;
  background: #fffaf3;
  border-radius: 28rpx 28rpx 0 0;
  padding: 28rpx 24rpx 40rpx;
}

.sheet-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.sheet-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #2a2118;
}

.close {
  color: #c87941;
  font-size: 28rpx;
}

.tabs {
  display: flex;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.tab {
  padding: 12rpx 20rpx;
  border-radius: 999rpx;
  background: #f1e7da;
  color: #7a6f63;
  font-size: 24rpx;
}

.tab.active {
  background: #c87941;
  color: #fff;
}

.options {
  max-height: 48vh;
}

.option {
  padding: 24rpx 8rpx;
  border-bottom: 1px solid #efe5d8;
  font-size: 30rpx;
  color: #2a2118;
}

.option.selected {
  color: #c87941;
  font-weight: 600;
}
</style>
