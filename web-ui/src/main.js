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

window.getUser = () => {
    return localStorage.getItem(USER_INFO) ? JSON.parse(localStorage.getItem(USER_INFO)) : null;
}

window.getUserTimeZone = () => {
    return Intl.DateTimeFormat().resolvedOptions().timeZone;
}

// 版本更新清理
const APP_VERSION_KEY = "app_version";
const APP_VERSION = "1.0.0"
const USER_INFO = "user_chat_netty";
window.USER_INFO = USER_INFO;
if (!localStorage.getItem(APP_VERSION_KEY) || localStorage.getItem(APP_VERSION_KEY) !== APP_VERSION) {
    localStorage.clear();
    localStorage.setItem(APP_VERSION_KEY, APP_VERSION);
}
//