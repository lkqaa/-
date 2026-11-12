import { defineStore } from 'pinia'
import { ref } from 'vue'
import http from '../api/http'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const adminInfo = ref(null)

  async function login(username, password) {
    const data = await http.post('/auth/login', { username, password })
    token.value = data.token
    adminInfo.value = data.adminInfo
    localStorage.setItem('token', data.token)
    return data
  }

  async function fetchInfo() {
    adminInfo.value = await http.get('/auth/info')
  }

  function logout() {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('token')
  }

  return { token, adminInfo, login, fetchInfo, logout }
})
