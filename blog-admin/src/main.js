import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import * as ElementPlusIcons from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import pinia from './stores/pinia'
import './style.css'

const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIcons)) {
  app.component(key, component)
}
app.use(ElementPlus)
app.use(router)
app.use(pinia)
app.mount('#app')
