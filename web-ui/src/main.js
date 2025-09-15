import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

const app = createApp(App);
app.use(store).use(router).mount('#app')

const HTTP_BASE_URL = process.env.VUE_APP_API_HTTP_BASE_URL;
window.HTTP_BASE_URL = HTTP_BASE_URL;
const WS_BASE_URL = process.env.VUE_APP_API_WS_BASE_URL;
window.WS_BASE_URL = WS_BASE_URL;