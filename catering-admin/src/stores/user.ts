import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('admin_token'))
  const username = ref(localStorage.getItem('admin_username') || '')
  const displayName = ref(localStorage.getItem('admin_display_name') || '')

  function setSession(value: { token: string; username: string; displayName: string }) {
    token.value = value.token
    username.value = value.username
    displayName.value = value.displayName
    localStorage.setItem('admin_token', value.token)
    localStorage.setItem('admin_username', value.username)
    localStorage.setItem('admin_display_name', value.displayName)
  }

  function clear() {
    token.value = null
    username.value = ''
    displayName.value = ''
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_username')
    localStorage.removeItem('admin_display_name')
  }

  return { token, username, displayName, setSession, clear }
})
