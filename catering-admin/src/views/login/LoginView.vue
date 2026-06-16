<template>
  <main class="login-page">
    <section class="characters-panel" aria-label="Animated login illustration">
      <div class="brand">
        <span class="brand-mark">
          <svg class="brand-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M9.937 15.5A2 2 0 0 0 8.5 14.063l-.581-1.743a1 1 0 0 0-1.898 0l-.581 1.743A2 2 0 0 0 4.063 15.5l-1.743.581a1 1 0 0 0 0 1.898l1.743.581A2 2 0 0 0 5.5 19.937l.581 1.743a1 1 0 0 0 1.898 0l.581-1.743A2 2 0 0 0 10.063 18.5l1.743-.581a1 1 0 0 0 0-1.898zM19 3l.35 1.05A2 2 0 0 0 20.95 5.65L22 6l-1.05.35A2 2 0 0 0 19.35 7.95L19 9l-.35-1.05A2 2 0 0 0 17.05 6.35L16 6l1.05-.35A2 2 0 0 0 18.65 4.05zM14 8l.45 1.35a2 2 0 0 0 1.2 1.2L17 11l-1.35.45a2 2 0 0 0-1.2 1.2L14 14l-.45-1.35a2 2 0 0 0-1.2-1.2L11 11l1.35-.45a2 2 0 0 0 1.2-1.2z" />
          </svg>
        </span>
        <span>餐饮信息采集管理端</span>
      </div>

      <div class="stage-wrap">
        <div class="stage" :class="{ 'stage-error': hasLoginError }">
          <div ref="purpleRef" class="character purple" :style="purpleStyle">
            <div class="eyes purple-eyes" :style="purpleEyesStyle">
              <EyeBall
                :size="18"
                :pupil-size="7"
                :max-distance="5"
                :is-blinking="isPurpleBlinking"
                :force-look-x="forcedPurpleLook('x')"
                :force-look-y="forcedPurpleLook('y')"
              />
              <EyeBall
                :size="18"
                :pupil-size="7"
                :max-distance="5"
                :is-blinking="isPurpleBlinking"
                :force-look-x="forcedPurpleLook('x')"
                :force-look-y="forcedPurpleLook('y')"
              />
            </div>
            <div class="character-mouth purple-mouth" :class="purpleMouthClass" :style="purpleMouthStyle"></div>
          </div>

          <div ref="blackRef" class="character black" :style="blackStyle">
            <div class="eyes black-eyes" :style="blackEyesStyle">
              <EyeBall
                :size="16"
                :pupil-size="6"
                :max-distance="4"
                :is-blinking="isBlackBlinking"
                :force-look-x="forcedBlackLook('x')"
                :force-look-y="forcedBlackLook('y')"
              />
              <EyeBall
                :size="16"
                :pupil-size="6"
                :max-distance="4"
                :is-blinking="isBlackBlinking"
                :force-look-x="forcedBlackLook('x')"
                :force-look-y="forcedBlackLook('y')"
              />
            </div>
          </div>

          <div ref="orangeRef" class="character orange" :style="orangeStyle">
            <div class="plain-eyes orange-eyes" :style="orangeEyesStyle">
              <Pupil :size="12" :max-distance="5" :force-look-x="forcedPlainLook('x')" :force-look-y="forcedPlainLook('y')" />
              <Pupil :size="12" :max-distance="5" :force-look-x="forcedPlainLook('x')" :force-look-y="forcedPlainLook('y')" />
            </div>
            <div class="character-mouth orange-mouth" :class="orangeMouthClass" :style="orangeMouthStyle"></div>
          </div>

          <div ref="yellowRef" class="character yellow" :style="yellowStyle">
            <div class="plain-eyes yellow-eyes" :style="yellowEyesStyle">
              <Pupil :size="12" :max-distance="5" :force-look-x="forcedPlainLook('x')" :force-look-y="forcedPlainLook('y')" />
              <Pupil :size="12" :max-distance="5" :force-look-x="forcedPlainLook('x')" :force-look-y="forcedPlainLook('y')" />
            </div>
            <div class="yellow-mouth" :style="yellowMouthStyle"></div>
          </div>
        </div>
      </div>

      <nav class="panel-links" aria-label="Footer links">
        <a href="#">隐私政策</a>
        <a href="#">服务条款</a>
        <a href="#">联系我们</a>
      </nav>
    </section>

    <section class="form-panel">
      <div class="form-card" :class="{ 'login-shake': hasLoginError }">
        <div class="mobile-brand">
          <span class="mobile-brand-mark">
            <svg class="brand-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M9.937 15.5A2 2 0 0 0 8.5 14.063l-.581-1.743a1 1 0 0 0-1.898 0l-.581 1.743A2 2 0 0 0 4.063 15.5l-1.743.581a1 1 0 0 0 0 1.898l1.743.581A2 2 0 0 0 5.5 19.937l.581 1.743a1 1 0 0 0 1.898 0l.581-1.743A2 2 0 0 0 10.063 18.5l1.743-.581a1 1 0 0 0 0-1.898zM19 3l.35 1.05A2 2 0 0 0 20.95 5.65L22 6l-1.05.35A2 2 0 0 0 19.35 7.95L19 9l-.35-1.05A2 2 0 0 0 17.05 6.35L16 6l1.05-.35A2 2 0 0 0 18.65 4.05zM14 8l.45 1.35a2 2 0 0 0 1.2 1.2L17 11l-1.35.45a2 2 0 0 0-1.2 1.2L14 14l-.45-1.35a2 2 0 0 0-1.2-1.2L11 11l1.35-.45a2 2 0 0 0 1.2-1.2z" />
            </svg>
          </span>
          <span>餐饮信息采集管理端</span>
        </div>

        <header class="form-header">
          <h1>Welcome back!</h1>
          <p>请输入你的账号信息</p>
        </header>

        <el-form class="login-form" @submit.prevent="handleLogin">
          <label class="field">
            <span>账号</span>
            <el-input
              v-model="form.username"
              placeholder="请输入管理员账号"
              autocomplete="username"
              @focus="isTyping = true"
              @blur="isTyping = false"
            />
          </label>

          <label class="field">
            <span>密码</span>
            <el-input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="••••••••"
              autocomplete="current-password"
              @focus="isTyping = true"
              @blur="isTyping = false"
              @keyup.enter="handleLogin"
            >
              <template #suffix>
                <button
                  class="icon-button"
                  type="button"
                  :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                  @click="showPassword = !showPassword"
                >
                  <svg v-if="!showPassword" class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M2.062 12.348a1 1 0 0 1 0-.696C3.432 7.874 7.016 5 12 5s8.568 2.874 9.938 6.652a1 1 0 0 1 0 .696C20.568 16.126 16.984 19 12 19s-8.568-2.874-9.938-6.652zM12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z" />
                  </svg>
                  <svg v-else class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M10.733 5.076A10.744 10.744 0 0 1 12 5c4.984 0 8.568 2.874 9.938 6.652a1 1 0 0 1 0 .696 10.58 10.58 0 0 1-2.044 3.19M14.084 14.158A3 3 0 0 1 9.842 9.916M17.479 17.499A10.75 10.75 0 0 1 12 19c-4.984 0-8.568-2.874-9.938-6.652a1 1 0 0 1 0-.696A10.6 10.6 0 0 1 6.18 6.279M2 2l20 20" />
                  </svg>
                </button>
              </template>
            </el-input>
          </label>

          <div class="form-row">
            <p class="hint">开发环境默认账号：admin / admin123</p>
          </div>

          <p v-if="error" class="error-message">{{ error }}</p>

          <el-button type="primary" class="primary-button" :loading="loading" @click="handleLogin">
            {{ loading ? '登录中...' : '进入工作台' }}
          </el-button>
        </el-form>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, inject, onMounted, onUnmounted, provide, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api/auth'
import { useUserStore } from '@/stores/user'

type MouseStore = {
  x: typeof mouseX
  y: typeof mouseY
}

const router = useRouter()
const userStore = useUserStore()

const showPassword = ref(false)
const loading = ref(false)
const error = ref('')
const form = reactive({ username: 'admin', password: '' })

const mouseX = ref(0)
const mouseY = ref(0)
const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const isTyping = ref(false)
const isLookingAtEachOther = ref(false)
const isPurplePeeking = ref(false)
const isLoginFailing = ref(false)

let loginFailTimer: number | undefined

const purpleRef = ref<HTMLElement | null>(null)
const blackRef = ref<HTMLElement | null>(null)
const yellowRef = ref<HTMLElement | null>(null)
const orangeRef = ref<HTMLElement | null>(null)
const MouseKey = Symbol('MousePosition')

provide<MouseStore>(MouseKey, { x: mouseX, y: mouseY })

const Pupil = defineComponent({
  name: 'Pupil',
  props: {
    size: { type: Number, default: 12 },
    maxDistance: { type: Number, default: 5 },
    pupilColor: { type: String, default: '#2d2d2d' },
    forceLookX: { type: Number, default: undefined },
    forceLookY: { type: Number, default: undefined },
  },
  setup(props) {
    const pupilRef = ref<HTMLElement | null>(null)
    const mouse = inject<MouseStore>(MouseKey)
    const position = computed(() => {
      if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
        return { x: props.forceLookX, y: props.forceLookY }
      }
      if (!pupilRef.value || !mouse) return { x: 0, y: 0 }
      const rect = pupilRef.value.getBoundingClientRect()
      const centerX = rect.left + rect.width / 2
      const centerY = rect.top + rect.height / 2
      const deltaX = mouse.x.value - centerX
      const deltaY = mouse.y.value - centerY
      const distance = Math.min(Math.sqrt(deltaX ** 2 + deltaY ** 2), props.maxDistance)
      const angle = Math.atan2(deltaY, deltaX)
      return { x: Math.cos(angle) * distance, y: Math.sin(angle) * distance }
    })

    return () =>
      h('div', {
        ref: pupilRef,
        class: 'pupil',
        style: {
          width: `${props.size}px`,
          height: `${props.size}px`,
          backgroundColor: props.pupilColor,
          transform: `translate(${position.value.x}px, ${position.value.y}px)`,
        },
      })
  },
})

const EyeBall = defineComponent({
  name: 'EyeBall',
  props: {
    size: { type: Number, default: 48 },
    pupilSize: { type: Number, default: 16 },
    maxDistance: { type: Number, default: 10 },
    eyeColor: { type: String, default: 'white' },
    pupilColor: { type: String, default: '#2d2d2d' },
    isBlinking: { type: Boolean, default: false },
    forceLookX: { type: Number, default: undefined },
    forceLookY: { type: Number, default: undefined },
  },
  setup(props) {
    const eyeRef = ref<HTMLElement | null>(null)
    const mouse = inject<MouseStore>(MouseKey)
    const position = computed(() => {
      if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
        return { x: props.forceLookX, y: props.forceLookY }
      }
      if (!eyeRef.value || !mouse) return { x: 0, y: 0 }
      const rect = eyeRef.value.getBoundingClientRect()
      const centerX = rect.left + rect.width / 2
      const centerY = rect.top + rect.height / 2
      const deltaX = mouse.x.value - centerX
      const deltaY = mouse.y.value - centerY
      const distance = Math.min(Math.sqrt(deltaX ** 2 + deltaY ** 2), props.maxDistance)
      const angle = Math.atan2(deltaY, deltaX)
      return { x: Math.cos(angle) * distance, y: Math.sin(angle) * distance }
    })

    return () =>
      h(
        'div',
        {
          ref: eyeRef,
          class: 'eye-ball',
          style: {
            width: `${props.size}px`,
            height: props.isBlinking ? '2px' : `${props.size}px`,
            backgroundColor: props.eyeColor,
          },
        },
        props.isBlinking
          ? []
          : [
              h('div', {
                class: 'pupil',
                style: {
                  width: `${props.pupilSize}px`,
                  height: `${props.pupilSize}px`,
                  backgroundColor: props.pupilColor,
                  transform: `translate(${position.value.x}px, ${position.value.y}px)`,
                },
              }),
            ],
      )
  },
})

function useWindowMouse() {
  const handleMouseMove = (event: MouseEvent) => {
    mouseX.value = event.clientX
    mouseY.value = event.clientY
  }

  onMounted(() => window.addEventListener('mousemove', handleMouseMove))
  onUnmounted(() => window.removeEventListener('mousemove', handleMouseMove))
}

function useRandomBlink(target: typeof isPurpleBlinking) {
  let timeoutId: number | undefined
  const scheduleBlink = () => {
    timeoutId = window.setTimeout(() => {
      target.value = true
      timeoutId = window.setTimeout(() => {
        target.value = false
        scheduleBlink()
      }, 150)
    }, Math.random() * 4000 + 3000)
  }

  onMounted(scheduleBlink)
  onUnmounted(() => window.clearTimeout(timeoutId))
}

useWindowMouse()
useRandomBlink(isPurpleBlinking)
useRandomBlink(isBlackBlinking)

onUnmounted(() => {
  window.clearTimeout(loginFailTimer)
})

watch(isTyping, (typing, _previous, onCleanup) => {
  if (!typing || isLoginFailing.value) {
    isLookingAtEachOther.value = false
    return
  }

  isLookingAtEachOther.value = true
  const timer = window.setTimeout(() => {
    isLookingAtEachOther.value = false
  }, 800)
  onCleanup(() => window.clearTimeout(timer))
})

watch([() => form.password, showPassword, isPurplePeeking, isLoginFailing], ([value, visible, _peeking, failing], _previous, onCleanup) => {
  if (!value.length || !visible || failing) {
    isPurplePeeking.value = false
    return
  }

  let resetTimer: number | undefined
  const timer = window.setTimeout(() => {
    isPurplePeeking.value = true
    resetTimer = window.setTimeout(() => {
      isPurplePeeking.value = false
    }, 800)
  }, Math.random() * 3000 + 2000)

  onCleanup(() => {
    window.clearTimeout(timer)
    window.clearTimeout(resetTimer)
  })
})

watch([() => form.username, () => form.password], () => {
  if (error.value) {
    error.value = ''
  }
})

const hasVisiblePassword = computed(() => form.password.length > 0 && showPassword.value)
const hasHiddenPassword = computed(() => form.password.length > 0 && !showPassword.value)
const hasLoginError = computed(() => isLoginFailing.value)

function calculatePosition(elementRef: typeof purpleRef) {
  const element = elementRef.value
  if (!element) return { faceX: 0, faceY: 0, bodySkew: 0 }

  const rect = element.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 3
  const deltaX = mouseX.value - centerX
  const deltaY = mouseY.value - centerY

  return {
    faceX: Math.max(-15, Math.min(15, deltaX / 20)),
    faceY: Math.max(-10, Math.min(10, deltaY / 30)),
    bodySkew: Math.max(-6, Math.min(6, -deltaX / 120)),
  }
}

const purplePos = computed(() => calculatePosition(purpleRef))
const blackPos = computed(() => calculatePosition(blackRef))
const yellowPos = computed(() => calculatePosition(yellowRef))
const orangePos = computed(() => calculatePosition(orangeRef))

const purpleStyle = computed(() => ({
  height: hasLoginError.value ? '400px' : isTyping.value || hasHiddenPassword.value ? '440px' : '400px',
  transform: hasLoginError.value
    ? 'skewX(-1deg) rotate(-4deg) translate(-8px, -4px)'
    : hasVisiblePassword.value
      ? 'skewX(0deg)'
      : isTyping.value || hasHiddenPassword.value
        ? `skewX(${purplePos.value.bodySkew - 12}deg) translateX(40px)`
        : `skewX(${purplePos.value.bodySkew}deg)`,
}))

const blackStyle = computed(() => ({
  transform: hasLoginError.value
    ? 'skewX(0deg) translate(4px, 4px)'
    : hasVisiblePassword.value
      ? 'skewX(0deg)'
      : isLookingAtEachOther.value
        ? `skewX(${blackPos.value.bodySkew * 1.5 + 10}deg) translateX(20px)`
        : isTyping.value || hasHiddenPassword.value
          ? `skewX(${blackPos.value.bodySkew * 1.5}deg)`
          : `skewX(${blackPos.value.bodySkew}deg)`,
}))

const orangeStyle = computed(() => ({
  transform: hasLoginError.value
    ? 'skewX(0deg) translateY(2px)'
    : hasVisiblePassword.value
      ? 'skewX(0deg)'
      : `skewX(${orangePos.value.bodySkew}deg)`,
}))

const yellowStyle = computed(() => ({
  transform: hasLoginError.value
    ? 'skewX(0deg) translate(5px, 0)'
    : hasVisiblePassword.value
      ? 'skewX(0deg)'
      : `skewX(${yellowPos.value.bodySkew}deg)`,
}))

const purpleEyesStyle = computed(() => ({
  left: hasLoginError.value ? '82px' : hasVisiblePassword.value ? '20px' : isLookingAtEachOther.value ? '55px' : `${45 + purplePos.value.faceX}px`,
  top: hasLoginError.value ? '56px' : hasVisiblePassword.value ? '35px' : isLookingAtEachOther.value ? '65px' : `${40 + purplePos.value.faceY}px`,
}))

const blackEyesStyle = computed(() => ({
  left: hasLoginError.value ? '36px' : hasVisiblePassword.value ? '10px' : isLookingAtEachOther.value ? '32px' : `${26 + blackPos.value.faceX}px`,
  top: hasLoginError.value ? '42px' : hasVisiblePassword.value ? '28px' : isLookingAtEachOther.value ? '12px' : `${32 + blackPos.value.faceY}px`,
}))

const orangeEyesStyle = computed(() => ({
  left: hasLoginError.value ? '104px' : hasVisiblePassword.value ? '50px' : `${82 + orangePos.value.faceX}px`,
  top: hasLoginError.value ? '98px' : hasVisiblePassword.value ? '85px' : `${90 + orangePos.value.faceY}px`,
}))

const yellowEyesStyle = computed(() => ({
  left: hasLoginError.value ? '58px' : hasVisiblePassword.value ? '20px' : `${52 + yellowPos.value.faceX}px`,
  top: hasLoginError.value ? '48px' : hasVisiblePassword.value ? '35px' : `${40 + yellowPos.value.faceY}px`,
}))

const yellowMouthStyle = computed(() => ({
  left: hasLoginError.value ? '36px' : hasVisiblePassword.value ? '10px' : `${40 + yellowPos.value.faceX}px`,
  top: hasLoginError.value ? '102px' : hasVisiblePassword.value ? '88px' : `${88 + yellowPos.value.faceY}px`,
}))

const purpleMouthStyle = computed(() => ({
  left: hasLoginError.value ? '94px' : hasVisiblePassword.value ? '32px' : isLookingAtEachOther.value ? '72px' : `${68 + purplePos.value.faceX}px`,
  top: hasLoginError.value ? '92px' : hasVisiblePassword.value ? '78px' : isLookingAtEachOther.value ? '104px' : `${92 + purplePos.value.faceY}px`,
}))

const orangeMouthStyle = computed(() => ({
  left: hasLoginError.value ? '124px' : hasVisiblePassword.value ? '78px' : `${92 + orangePos.value.faceX}px`,
  top: hasLoginError.value ? '146px' : hasVisiblePassword.value ? '126px' : `${126 + orangePos.value.faceY}px`,
}))

const purpleMouthClass = computed(() => ({
  'mouth-error': hasLoginError.value,
  'mouth-typing': isTyping.value || hasHiddenPassword.value,
  'mouth-peeking': hasVisiblePassword.value,
  'mouth-sneaky': hasVisiblePassword.value && isPurplePeeking.value,
  'mouth-looking': isLookingAtEachOther.value,
}))

const orangeMouthClass = computed(() => ({
  'mouth-error': hasLoginError.value,
  'mouth-typing': isTyping.value || hasHiddenPassword.value,
  'mouth-peeking': hasVisiblePassword.value,
}))

function forcedPurpleLook(axis: 'x' | 'y') {
  if (hasLoginError.value) return axis === 'x' ? 4 : -3
  if (hasVisiblePassword.value) return isPurplePeeking.value ? (axis === 'x' ? 4 : 5) : -4
  if (isLookingAtEachOther.value) return axis === 'x' ? 3 : 4
  return undefined
}

function forcedBlackLook(axis: 'x' | 'y') {
  if (hasLoginError.value) return axis === 'x' ? 3 : -3
  if (hasVisiblePassword.value) return -4
  if (isLookingAtEachOther.value) return axis === 'x' ? 0 : -4
  return undefined
}

function forcedPlainLook(axis: 'x' | 'y') {
  if (hasLoginError.value) return axis === 'x' ? 4 : -3
  return hasVisiblePassword.value ? (axis === 'x' ? -5 : -4) : undefined
}

async function handleLogin() {
  if (!form.username || !form.password) {
    error.value = '请输入账号和密码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const res = await adminLogin(form.username, form.password)
    const data = res.data.data
    userStore.setSession({
      token: data.token,
      username: data.username,
      displayName: data.displayName,
    })
    ElMessage.success(`欢迎回来，${data.displayName || data.username}`)
    router.push('/')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败'
    isLoginFailing.value = false
    window.clearTimeout(loginFailTimer)
    window.requestAnimationFrame(() => {
      isLoginFailing.value = true
      loginFailTimer = window.setTimeout(() => {
        isLoginFailing.value = false
      }, 1250)
    })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: #ffffff;
  color: #09090b;
  font-family: 'Noto Sans SC', 'Segoe UI', sans-serif;
}

.characters-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
  min-height: 100vh;
  padding: 48px;
  color: #fafafa;
  background:
    radial-gradient(circle at 72% 28%, rgb(255 255 255 / 0.1), transparent 0 128px, transparent 220px),
    radial-gradient(circle at 28% 75%, rgb(255 255 255 / 0.06), transparent 0 192px, transparent 310px),
    linear-gradient(135deg, rgb(23 23 23 / 0.9), #171717 45%, rgb(23 23 23 / 0.82));
}

.characters-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgb(255 255 255 / 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgb(255 255 255 / 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
}

.brand,
.panel-links,
.stage-wrap {
  position: relative;
  z-index: 2;
}

.brand,
.mobile-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.brand-mark,
.mobile-brand-mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: grid;
  place-items: center;
}

.brand-mark {
  background: rgb(255 255 255 / 0.1);
  backdrop-filter: blur(8px);
}

.mobile-brand-mark {
  color: #171717;
  background: rgb(23 23 23 / 0.1);
}

.brand-icon {
  width: 16px;
  height: 16px;
}

.stage-wrap {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 500px;
  overflow: hidden;
  padding-bottom: 10px;
}

.stage {
  position: relative;
  width: 550px;
  height: 400px;
  flex: none;
}

.stage-error {
  animation: stage-error-wobble 460ms cubic-bezier(0.36, 0.07, 0.19, 0.97);
}

.character {
  position: absolute;
  bottom: 0;
  transform-origin: bottom center;
  transition: height 700ms ease-in-out, transform 820ms cubic-bezier(0.2, 0.82, 0.22, 1);
}

.purple {
  left: 70px;
  z-index: 1;
  width: 180px;
  background: #6c3ff5;
  border-radius: 10px 10px 0 0;
}

.black {
  left: 240px;
  z-index: 2;
  width: 120px;
  height: 310px;
  background: #2d2d2d;
  border-radius: 8px 8px 0 0;
}

.orange {
  left: 0;
  z-index: 3;
  width: 240px;
  height: 200px;
  background: #ff9b6b;
  border-radius: 120px 120px 0 0;
}

.yellow {
  left: 310px;
  z-index: 4;
  width: 140px;
  height: 230px;
  background: #e8d754;
  border-radius: 70px 70px 0 0;
}

.eyes,
.plain-eyes {
  position: absolute;
  display: flex;
  transition: left 700ms cubic-bezier(0.2, 0.82, 0.22, 1), top 700ms cubic-bezier(0.2, 0.82, 0.22, 1);
}

.purple-eyes {
  gap: 32px;
}

.black-eyes,
.yellow-eyes {
  gap: 24px;
}

.orange-eyes {
  gap: 32px;
}

.plain-eyes {
  transition-duration: 200ms;
  transition-timing-function: ease-out;
}

.eye-ball {
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 150ms ease;
}

.pupil {
  border-radius: 999px;
  transition: transform 100ms ease-out;
}

.character-mouth,
.yellow-mouth {
  position: absolute;
  transition:
    left 700ms cubic-bezier(0.2, 0.82, 0.22, 1),
    top 700ms cubic-bezier(0.2, 0.82, 0.22, 1),
    width 520ms ease,
    height 520ms ease,
    border 520ms ease,
    background-color 520ms ease,
    transform 700ms cubic-bezier(0.2, 0.82, 0.22, 1);
}

.yellow-mouth {
  width: 80px;
  height: 4px;
  border-radius: 999px;
  background: #2d2d2d;
}

.stage-error .yellow-mouth {
  width: 44px;
  height: 18px;
  background: transparent;
  border-top: 5px solid #2d2d2d;
  border-radius: 999px 999px 0 0;
  transform: rotate(-4deg);
}

.purple-mouth {
  width: 42px;
  height: 4px;
  border-radius: 999px;
  background: #241842;
  transform-origin: center;
}

.orange-mouth {
  width: 56px;
  height: 4px;
  border-radius: 999px;
  background: #2d2d2d;
  transform-origin: center;
  transition-duration: 200ms;
  transition-timing-function: ease-out;
}

.purple-mouth.mouth-typing {
  width: 32px;
  height: 8px;
  transform: rotate(-8deg);
}

.purple-mouth.mouth-looking {
  width: 26px;
  height: 6px;
  transform: rotate(8deg);
}

.purple-mouth.mouth-peeking {
  width: 22px;
  height: 22px;
  border-radius: 999px;
  background: #241842;
  transform: scale(0.8);
}

.purple-mouth.mouth-sneaky {
  transform: translate(8px, 5px) scale(0.95);
}

.purple-mouth.mouth-error,
.orange-mouth.mouth-error {
  background: transparent;
  border-top: 5px solid currentColor;
  border-radius: 999px 999px 0 0;
}

.purple-mouth.mouth-error {
  width: 28px;
  height: 16px;
  color: #241842;
  transform: rotate(-6deg);
}

.orange-mouth.mouth-typing {
  width: 42px;
  height: 6px;
  transform: rotate(4deg);
}

.orange-mouth.mouth-peeking {
  width: 34px;
  height: 6px;
  transform: rotate(-10deg);
}

.orange-mouth.mouth-error {
  width: 36px;
  height: 16px;
  color: #2d2d2d;
  transform: rotate(3deg);
}

.panel-links {
  display: flex;
  gap: 32px;
  color: rgb(250 250 250 / 0.6);
  font-size: 14px;
}

.panel-links a {
  transition: color 160ms ease;
}

.panel-links a:hover {
  color: #fafafa;
}

.form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 32px;
  background: #ffffff;
}

.form-card {
  width: 100%;
  max-width: 420px;
}

.login-shake {
  animation: login-shake 460ms cubic-bezier(0.36, 0.07, 0.19, 0.97);
}

.mobile-brand {
  display: none;
  justify-content: center;
  margin-bottom: 48px;
}

.form-header {
  margin-bottom: 40px;
  text-align: center;
}

.form-header h1 {
  margin: 0 0 8px;
  font-size: 30px;
  line-height: 1.2;
  font-weight: 700;
}

.form-header p {
  margin: 0;
  color: #71717a;
  font-size: 14px;
}

.login-form {
  display: grid;
  gap: 20px;
}

.field {
  display: grid;
  gap: 8px;
  color: #09090b;
  font-size: 14px;
  font-weight: 500;
}

.login-form :deep(.el-input__wrapper) {
  min-height: 48px;
  border-radius: 6px;
  box-shadow: 0 0 0 1px rgb(228 228 231 / 0.8) inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #171717 inset, 0 0 0 3px rgb(23 23 23 / 0.1);
}

.login-form :deep(.el-input__inner) {
  color: #09090b;
}

.icon-button {
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  padding: 0;
  border: 0;
  color: #71717a;
  background: transparent;
  cursor: pointer;
  transition: color 150ms ease;
}

.icon-button:hover {
  color: #09090b;
}

.eye-icon {
  width: 18px;
  height: 18px;
}

.form-row {
  display: flex;
  justify-content: flex-start;
}

.hint {
  margin: 0;
  color: #71717a;
  font-size: 13px;
}

.error-message {
  margin: 0;
  padding: 12px;
  color: #dc2626;
  background: rgb(254 242 242);
  border: 1px solid rgb(254 202 202);
  border-radius: 8px;
  font-size: 14px;
}

.primary-button {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  background: #171717;
}

.primary-button:hover {
  background: #27272a;
}

.primary-button.is-loading,
.primary-button[disabled] {
  opacity: 0.75;
}

@media (max-width: 1023px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .characters-panel {
    display: none;
  }

  .mobile-brand {
    display: flex;
  }
}

@media (max-width: 480px) {
  .form-panel {
    padding: 24px;
  }
}

@keyframes login-shake {
  0%,
  100% {
    transform: translateX(0);
  }

  20% {
    transform: translateX(-10px);
  }

  40% {
    transform: translateX(8px);
  }

  60% {
    transform: translateX(-6px);
  }

  80% {
    transform: translateX(4px);
  }
}

@keyframes stage-error-wobble {
  0% {
    transform: translateX(0) rotate(0deg);
  }

  20% {
    transform: translateX(-6px) rotate(-0.8deg);
  }

  42% {
    transform: translateX(5px) rotate(0.6deg);
  }

  64% {
    transform: translateX(-3px) rotate(-0.4deg);
  }

  82% {
    transform: translateX(2px) rotate(0.2deg);
  }

  100% {
    transform: translateX(0) rotate(0deg);
  }
}
</style>
