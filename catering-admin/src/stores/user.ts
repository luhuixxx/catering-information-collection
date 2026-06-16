import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('admin_token'))
  const username = ref('')

  function setToken(value: string) {
    token.value = value
    localStorage.setItem('admin_token', value)
  }

  function clear() {
    token.value = null
    username.value = ''
    localStorage.removeItem('admin_token')
  }

  return { token, username, setToken, clear }
})
