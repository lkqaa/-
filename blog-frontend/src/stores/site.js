import { defineStore } from 'pinia'
import { ref } from 'vue'
import { siteApi } from '../api'

export const useSiteStore = defineStore('site', () => {
  const config = ref({ blogTitle: '', blogSubtitle: '', blogLogo: '', aboutMe: '' })
  let loaded = false
  let loading = null

  async function loadConfig() {
    if (loaded) return config.value
    if (!loading) {
      loading = siteApi.config().then(data => {
        config.value = { ...config.value, ...data }
        loaded = true
        return config.value
      }).finally(() => {
        loading = null
      })
    }
    return loading
  }

  return { config, loadConfig }
})
