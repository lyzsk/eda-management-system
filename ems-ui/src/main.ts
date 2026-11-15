import { createApp } from "vue";
import { createPinia } from "pinia";
import piniaPluginPersistedstate from "pinia-plugin-persistedstate";
import ElementPlus from "element-plus";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import "element-plus/dist/index.css";
import "element-plus/theme-chalk/dark/css-vars.css";
import App from "./App.vue";
import router from "./router";
import i18n from "./langs";
import { getMe } from "./api/auth";
import { useAppStore } from "./store/modules/app";
import { useUserStore } from "./store/modules/user";
import "./assets/styles/index.scss";

const app = createApp(App);

const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);
app.use(pinia);

useAppStore(pinia).applyTheme();

app.use(router);
app.use(i18n);
app.use(ElementPlus, { locale: zhCn });

app.mount("#app");

const userStore = useUserStore(pinia);
if (userStore.isLogin) {
    getMe()
        .then((user) => userStore.setUser(user))
        .catch(() => {});
}
