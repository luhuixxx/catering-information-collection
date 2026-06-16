<template>
  <view class="page">
    <scroll-view class="chat" scroll-y :scroll-into-view="bottomAnchor">
      <view class="intro">
        <text class="kicker">AI 搜索</text>
        <text class="title">告诉我你想找什么</text>
        <text class="subtitle">比如“杭州大厨 8000 以上”或“可明火的铺面”。</text>
      </view>

      <view class="examples" v-if="messages.length === 0">
        <button v-for="item in examples" :key="item" class="example" @click="sendExample(item)">{{ item }}</button>
      </view>

      <view class="history-panel" v-if="messages.length === 0 && history.length">
        <view class="section-head">
          <text class="section-title">最近搜索</text>
          <button class="clear-history" @click="clearHistory">清空</button>
        </view>
        <view class="history-list">
          <view v-for="item in history" :key="item" class="history-item">
            <button class="history-query" @click="sendHistory(item)">{{ item }}</button>
            <button class="history-delete" @click.stop="deleteHistory(item)">删除</button>
          </view>
        </view>
      </view>

      <view v-for="message in messages" :key="message.id" :id="message.id" :class="['bubble-row', message.role]">
        <view :class="['bubble', message.role]">
          <text class="bubble-text">{{ message.content }}</text>
          <view v-if="message.filters?.length" class="filter-chips">
            <text v-for="item in message.filters" :key="item" class="filter-chip">{{ item }}</text>
          </view>
          <view v-if="message.cards?.length" class="card-list">
            <view v-for="card in message.cards" :key="card.id" class="post-card" @click="goDetail(card.id)">
              <image v-if="card.coverImage" class="cover" :src="card.coverImage" mode="aspectFill" />
              <view class="content">
                <view class="line">
                  <text class="badge">{{ labelOf(card.postType) }}</text>
                  <text v-if="card.isTop === 1" class="top">置顶</text>
                </view>
                <text class="post-title">{{ card.title }}</text>
                <text class="summary">{{ card.summary }}</text>
                <view class="meta">
                  <text>{{ card.cityName }} · {{ card.districtName }}</text>
                  <text>{{ dateText(card.createdAt) }}</text>
                </view>
              </view>
            </view>
          </view>
          <button v-if="message.showFilter" class="secondary" @click="goFilterSearch(message.query || '')">改用筛选搜索</button>
        </view>
      </view>

      <view v-if="loading" class="bubble-row assistant" id="loading">
        <view class="bubble assistant"><text class="bubble-text">正在理解你的需求...</text></view>
      </view>
      <view id="bottom" />
    </scroll-view>

    <view class="composer">
      <textarea
        v-model="query"
        class="prompt"
        placeholder="输入你要找的信息"
        maxlength="100"
        auto-height
        confirm-type="send"
      />
      <button class="send" :loading="loading" @click="sendMessage">发送</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { nextTick, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { aiSearch, type PublicPostItem } from "@/api/post";

interface ChatMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
  query?: string;
  cards?: PublicPostItem[];
  filters?: string[];
  showFilter?: boolean;
}

const query = ref("");
const cityId = ref<number | undefined>();
const districtId = ref<number | undefined>();
const loading = ref(false);
const messages = ref<ChatMessage[]>([]);
const history = ref<string[]>([]);
const bottomAnchor = ref("bottom");
const examples = ["杭州大厨 8000 以上", "可明火铺面", "宁波火锅服务员", "餐饮店转让"];
const historyKey = "search_history";

function sendExample(value: string) {
  query.value = value;
  sendMessage();
}

function sendHistory(value: string) {
  query.value = value;
  sendMessage();
}

async function sendMessage() {
  const text = query.value.trim();
  if (!text || loading.value) {
    if (!text) uni.showToast({ title: "请输入搜索内容", icon: "none" });
    return;
  }
  query.value = "";
  messages.value.push({ id: nextId("user"), role: "user", content: text });
  saveHistory(text);
  scrollToBottom();
  loading.value = true;
  try {
    const res = await aiSearch({
      query: text,
      cityId: cityId.value,
      districtId: districtId.value,
      page: 1,
      size: 8,
      messages: messages.value.slice(-8).map((item) => ({ role: item.role, content: item.content })),
    });
    const cards = res.data.cards?.length ? res.data.cards : res.data.list.records.slice(0, 5);
    const fallbackText = cards.length ? `我找到了 ${res.data.list.total} 条相关信息，先给你看这几条。` : "暂时没找到匹配信息，可以换个说法或放宽地区/薪资。";
    const replyText = cards.length ? res.data.reply || fallbackText : fallbackText;
    messages.value.push({
      id: nextId("assistant"),
      role: "assistant",
      content: replyText,
      query: text,
      cards,
      filters: filterChips(res.data.parsedFilters || {}),
      showFilter: res.data.degraded || cards.length === 0,
    });
  } catch (e) {
    messages.value.push({
      id: nextId("assistant"),
      role: "assistant",
      content: "没太听懂，请试试筛选或换个说法。",
      query: text,
      showFilter: true,
    });
    uni.showToast({ title: e instanceof Error ? e.message : "搜索失败", icon: "none" });
  } finally {
    loading.value = false;
    scrollToBottom();
  }
}

function filterChips(filters: Record<string, unknown>) {
  const chips: string[] = [];
  const type = textOf(filters.postType);
  if (type) chips.push(labelOf(type));
  if (filters.cityId || cityId.value) chips.push("已选城市");
  if (filters.districtId || districtId.value) chips.push("已选区县");
  appendRange(chips, "薪资", filters.minSalary, filters.maxSalary);
  appendText(chips, "岗位", filters.jobRole);
  appendText(chips, "类型", filters.shopCategory);
  if (filters.canCatering === true) chips.push("可餐饮");
  if (filters.canOpenFlame === true) chips.push("可明火");
  return chips;
}

function goFilterSearch(text: string) {
  const params: string[] = [];
  if (text) params.push(`keyword=${encodeURIComponent(text)}`);
  if (cityId.value) params.push(`cityId=${cityId.value}`);
  if (districtId.value) params.push(`districtId=${districtId.value}`);
  uni.navigateTo({ url: `/pages/list/list${params.length ? `?${params.join("&")}` : ""}` });
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` });
}

function labelOf(type: string) {
  const map: Record<string, string> = {
    RECRUIT: "招聘",
    TRANSFER: "转让",
    RENT: "出租",
    JOB_SEEK: "求职",
    FRANCHISE: "加盟",
  };
  return map[type] || "信息";
}

function dateText(value: string) {
  return value ? value.replace("T", " ").slice(5, 16) : "";
}

function appendText(chips: string[], label: string, value: unknown) {
  const text = textOf(value);
  if (text) chips.push(`${label} ${text}`);
}

function appendRange(chips: string[], label: string, min: unknown, max: unknown) {
  const minText = textOf(min);
  const maxText = textOf(max);
  if (minText && maxText) chips.push(`${label} ${minText}-${maxText}`);
  else if (minText) chips.push(`${label} ${minText}+`);
  else if (maxText) chips.push(`${label} ${maxText}以内`);
}

function textOf(value: unknown) {
  return value === undefined || value === null || value === "" ? "" : String(value);
}

function nextId(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function scrollToBottom() {
  nextTick(() => {
    bottomAnchor.value = "loading";
    setTimeout(() => {
      bottomAnchor.value = "bottom";
    }, 30);
  });
}

function saveHistory(text: string) {
  history.value = [text, ...history.value.filter((item) => item !== text)].slice(0, 10);
  uni.setStorageSync(historyKey, history.value);
}

function loadHistory() {
  const stored = uni.getStorageSync(historyKey);
  history.value = Array.isArray(stored) ? stored.filter((item) => typeof item === "string" && item.trim()).slice(0, 10) : [];
}

function deleteHistory(text: string) {
  history.value = history.value.filter((item) => item !== text);
  uni.setStorageSync(historyKey, history.value);
}

function clearHistory() {
  history.value = [];
  uni.removeStorageSync(historyKey);
}

onLoad((params) => {
  loadHistory();
  if (typeof params?.query === "string") query.value = decodeURIComponent(params.query);
  if (typeof params?.cityId === "string") cityId.value = Number(params.cityId);
  if (typeof params?.districtId === "string") districtId.value = Number(params.districtId);
  if (query.value) sendMessage();
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f7f2ea;
  display: flex;
  flex-direction: column;
}

.chat {
  flex: 1;
  height: calc(100vh - 150rpx);
  padding: 32rpx 28rpx 18rpx;
  box-sizing: border-box;
}

.intro {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 18rpx;
}

.kicker {
  color: #a85a24;
  font-size: 23rpx;
}

.title {
  color: #2a2118;
  font-size: 42rpx;
  font-weight: 800;
}

.subtitle {
  color: #8f8578;
  font-size: 25rpx;
}

.examples,
.filter-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.history-panel {
  margin-top: 20rpx;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.section-title {
  color: #66594d;
  font-size: 25rpx;
  font-weight: 700;
}

.clear-history {
  height: 52rpx;
  line-height: 52rpx;
  margin: 0;
  padding: 0 18rpx;
  border-radius: 12rpx;
  background: #fff;
  color: #8f8578;
  font-size: 23rpx;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.history-query {
  flex: 1;
  min-width: 0;
  height: 68rpx;
  line-height: 68rpx;
  margin: 0;
  padding: 0 18rpx;
  border-radius: 12rpx;
  background: #fff;
  color: #2a2118;
  font-size: 25rpx;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-delete {
  flex-shrink: 0;
  width: 92rpx;
  height: 68rpx;
  line-height: 68rpx;
  margin: 0;
  padding: 0;
  border-radius: 12rpx;
  background: #f1e7da;
  color: #7a4a25;
  font-size: 23rpx;
}

.example,
.filter-chip {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #f1e7da;
  color: #7a4a25;
  font-size: 23rpx;
}

.bubble-row {
  display: flex;
  margin-top: 18rpx;
}

.bubble-row.user {
  justify-content: flex-end;
}

.bubble-row.assistant {
  justify-content: flex-start;
}

.bubble {
  max-width: 86%;
  padding: 18rpx;
  border-radius: 18rpx;
}

.bubble.user {
  background: #2a2118;
  color: #fff6ea;
}

.bubble.assistant {
  background: #fff;
  color: #2a2118;
  box-shadow: 0 12rpx 30rpx rgba(42, 33, 24, 0.05);
}

.bubble-text {
  font-size: 28rpx;
  line-height: 1.55;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-top: 16rpx;
}

.post-card {
  display: flex;
  gap: 16rpx;
  padding: 16rpx;
  border-radius: 14rpx;
  background: #faf5ee;
}

.cover {
  width: 132rpx;
  height: 132rpx;
  border-radius: 12rpx;
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
  color: #2a2118;
  font-size: 28rpx;
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

.secondary {
  width: 100%;
  height: 72rpx;
  line-height: 72rpx;
  margin-top: 16rpx;
  border-radius: 14rpx;
  background: #f1e7da;
  color: #2a2118;
  font-size: 25rpx;
}

.composer {
  display: flex;
  gap: 12rpx;
  padding: 18rpx 22rpx 26rpx;
  background: #fff;
  box-shadow: 0 -8rpx 24rpx rgba(42, 33, 24, 0.06);
}

.prompt {
  flex: 1;
  min-height: 78rpx;
  max-height: 160rpx;
  padding: 18rpx 20rpx;
  border-radius: 16rpx;
  background: #faf5ee;
  color: #2a2118;
  font-size: 27rpx;
  box-sizing: border-box;
}

.send {
  width: 132rpx;
  height: 78rpx;
  line-height: 78rpx;
  border-radius: 16rpx;
  background: #a85a24;
  color: #fff;
  font-size: 27rpx;
}
</style>
