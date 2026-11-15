<template>
    <div class="login-page" :class="{ 'is-dark': appStore.isDark }">
        <section class="login-left">
            <div class="left-content">
                <div class="left-logo">
                    <img src="/apple-touch-icon.png" alt="EDA 管理平台" />
                </div>
                <h1>{{ t("login.leftTitle") }}</h1>
                <p>{{ t("login.leftSubtitle") }}</p>
                <div class="illustration">
                    <img :src="loginIcon" alt="" />
                </div>
            </div>

            <span class="geo circle-one"></span>
            <span class="geo circle-two"></span>
            <span class="geo square-one"></span>
            <span class="geo square-two"></span>
            <span class="geo dot-one"></span>
            <span class="geo dot-two"></span>
        </section>

        <section class="login-right">
            <div class="login-actions">
                <button
                    class="icon-btn"
                    :title="
                        appStore.locale === 'zh-CN' ? 'English' : '简体中文'
                    "
                    :aria-label="
                        appStore.locale === 'zh-CN' ? 'English' : '简体中文'
                    "
                    @click="toggleLocale"
                >
                    <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path
                            fill="currentColor"
                            d="m18.5 10l4.4 11h-2.155l-1.201-3h-4.09l-1.199 3h-2.154L16.5 10zM10 2v2h6v2h-1.968a18.2 18.2 0 0 1-3.62 6.301a15 15 0 0 0 2.335 1.707l-.75 1.878A17 17 0 0 1 9 13.725a16.7 16.7 0 0 1-6.201 3.548l-.536-1.929a14.7 14.7 0 0 0 5.327-3.042A18 18 0 0 1 4.767 8h2.24A16 16 0 0 0 9 10.877a16.2 16.2 0 0 0 2.91-4.876L2 6V4h6V2zm7.5 10.885L16.253 16h2.492z"
                        />
                    </svg>
                </button>
                <button
                    class="icon-btn theme-btn"
                    :title="
                        appStore.isDark ? t('header.light') : t('header.dark')
                    "
                    :aria-label="
                        appStore.isDark ? t('header.light') : t('header.dark')
                    "
                    @click="toggleTheme"
                >
                    <svg
                        v-if="appStore.isDark"
                        viewBox="0 0 24 24"
                        aria-hidden="true"
                    >
                        <path
                            fill="currentColor"
                            d="M12 18a6 6 0 1 1 0-12a6 6 0 0 1 0 12M11 1h2v3h-2zm0 19h2v3h-2zM3.515 4.929l1.414-1.414L7.05 5.636L5.636 7.05zM16.95 18.364l1.414-1.414l2.121 2.121l-1.414 1.414zm2.121-14.85l1.414 1.415l-2.121 2.121l-1.414-1.414zM5.636 16.95l1.414 1.414l-2.121 2.121l-1.414-1.414zM23 11v2h-3v-2zM4 11v2H1v-2z"
                        />
                    </svg>
                    <svg v-else viewBox="0 0 24 24" aria-hidden="true">
                        <path
                            fill="currentColor"
                            d="M10 7a7 7 0 0 0 12 4.9v.1c0 5.523-4.477 10-10 10S2 17.523 2 12S6.477 2 12 2h.1A6.98 6.98 0 0 0 10 7m-6 5a8 8 0 0 0 15.062 3.762A9 9 0 0 1 8.238 4.938A8 8 0 0 0 4 12"
                        />
                    </svg>
                </button>
            </div>

            <div class="login-card">
                <h2>{{ t("login.title") }}</h2>
                <p class="login-subtitle">{{ t("login.subtitle") }}</p>

                <el-form
                    ref="formRef"
                    :model="form"
                    :rules="rules"
                    size="large"
                    @keyup.enter="submit"
                >
                    <el-form-item prop="username">
                        <el-input
                            v-model.trim="form.username"
                            :placeholder="t('login.usernamePlaceholder')"
                            autocomplete="username"
                        >
                            <template #prefix>
                                <el-icon><User /></el-icon>
                            </template>
                        </el-input>
                    </el-form-item>

                    <el-form-item prop="password">
                        <el-input
                            v-model.trim="form.password"
                            type="password"
                            show-password
                            :placeholder="t('login.passwordPlaceholder')"
                            autocomplete="current-password"
                        >
                            <template #prefix>
                                <el-icon><Lock /></el-icon>
                            </template>
                        </el-input>
                    </el-form-item>

                    <el-button
                        class="login-btn"
                        type="primary"
                        :loading="loading"
                        @click="submit"
                    >
                        {{ loading ? t("login.loading") : t("login.button") }}
                    </el-button>
                </el-form>
            </div>
        </section>
    </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import {
    ElMessage,
    ElNotification,
    type FormInstance,
    type FormRules,
} from "element-plus";
import { Lock, User } from "@element-plus/icons-vue";
import loginIcon from "@/assets/images/login/login_icon.svg";
import { login } from "@/api/auth";
import { useAppStore } from "@/store/modules/app";
import { useUserStore } from "@/store/modules/user";
import { toggleThemeWithAnimation } from "@/utils/themeAnimation";

const { t } = useI18n();
const appStore = useAppStore();
const userStore = useUserStore();
const router = useRouter();
const route = useRoute();

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
    username: "admin",
    password: "",
});

const rules = computed<FormRules>(() => ({
    username: [
        {
            required: true,
            message: t("login.usernamePlaceholder"),
            trigger: "blur",
        },
    ],
    password: [
        {
            required: true,
            message: t("login.passwordPlaceholder"),
            trigger: "blur",
        },
    ],
}));

function toggleLocale() {
    appStore.setLocale(appStore.locale === "zh-CN" ? "en" : "zh-CN");
}

function toggleTheme(event: MouseEvent) {
    toggleThemeWithAnimation(event, () => appStore.toggleTheme());
}

async function submit() {
    if (!formRef.value) {
        return;
    }

    const valid = await formRef.value.validate().catch(() => false);
    if (!valid) {
        return;
    }

    loading.value = true;
    try {
        const result = await login({
            username: form.username,
            password: form.password,
        });

        userStore.setLogin(result);

        ElNotification({
            title: t("login.success"),
            message: `${t("login.welcome")}, ${t("app.name")}!`,
            type: "success",
            duration: 2500,
        });

        const redirect =
            typeof route.query.redirect === "string"
                ? route.query.redirect
                : "/dashboard";
        await router.replace(redirect);
    } catch (error) {
        ElMessage.error(
            error instanceof Error ? error.message : t("login.error"),
        );
    } finally {
        loading.value = false;
    }
}
</script>

<style scoped lang="scss">
.login-page {
    display: flex;
    min-height: 100vh;
    overflow: hidden;
    background: var(--app-bg);
}

.login-left {
    position: relative;
    display: flex;
    flex: 1 1 58%;
    flex-direction: column;
    overflow: hidden;
    background:
        radial-gradient(
            circle at 18% 18%,
            rgb(93 135 255 / 18%),
            transparent 38%
        ),
        radial-gradient(
            circle at 88% 78%,
            rgb(124 196 255 / 20%),
            transparent 42%
        ),
        linear-gradient(135deg, #eef3ff 0%, #e3edff 52%, #edf6ff 100%);
}

.login-page.is-dark .login-left {
    background:
        radial-gradient(
            circle at 18% 18%,
            rgb(93 135 255 / 22%),
            transparent 38%
        ),
        radial-gradient(
            circle at 88% 78%,
            rgb(56 189 248 / 14%),
            transparent 42%
        ),
        linear-gradient(135deg, #0d1220 0%, #101827 52%, #0b1322 100%);
}

.left-content {
    position: relative;
    z-index: 3;
    width: min(560px, 82%);
    margin: auto;
    padding: 80px 0 70px;
}

.left-logo {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 72px;
    height: 72px;
    padding: 8px;
    margin-bottom: 26px;
    border-radius: 18px;
    background: #fff;
    box-shadow: 0 10px 30px rgb(15 23 42 / 10%);
}

.left-logo img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: contain;
}

.left-content h1 {
    margin: 0;
    color: var(--text);
    font-size: 42px;
    line-height: 1.18;
    letter-spacing: -1px;
}

.left-content p {
    max-width: 460px;
    margin: 16px 0 0;
    color: var(--text-muted);
    font-size: 16px;
    line-height: 1.7;
}

.illustration {
    width: min(390px, 80%);
    margin-top: 38px;
}

.illustration img {
    display: block;
    width: 100%;
    height: auto;
}

.geo {
    position: absolute;
    pointer-events: none;
}

.circle-one {
    top: 16%;
    left: 8%;
    width: 56px;
    height: 56px;
    border: 2px solid rgb(93 135 255 / 28%);
    border-radius: 50%;
}

.circle-two {
    bottom: 14%;
    left: 16%;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: rgb(93 135 255 / 22%);
}

.square-one {
    right: 10%;
    bottom: 12%;
    width: 82px;
    height: 82px;
    border-radius: 18px;
    background: rgb(93 135 255 / 12%);
    transform: rotate(18deg);
}

.square-two {
    top: 46%;
    right: 14%;
    width: 42px;
    height: 42px;
    border-radius: 12px;
    background: rgb(93 135 255 / 8%);
    transform: rotate(-12deg);
}

.dot-one {
    top: 22%;
    right: 24%;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: rgb(93 135 255 / 30%);
}

.dot-two {
    bottom: 26%;
    left: 34%;
    width: 9px;
    height: 9px;
    border-radius: 50%;
    background: rgb(93 135 255 / 24%);
}

.login-right {
    position: relative;
    display: flex;
    flex: 1 1 42%;
    align-items: center;
    justify-content: center;
    padding: 48px;
    background: var(--panel-bg);
}

.login-actions {
    position: absolute;
    top: 22px;
    right: 26px;
    display: flex;
    gap: 6px;
}

.icon-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    padding: 0;
    border: 0;
    border-radius: 8px;
    background: transparent;
    color: var(--text-muted);
    cursor: pointer;
    transition:
        background 0.2s,
        color 0.2s;
}

.icon-btn:hover {
    background: var(--slider-bg);
    color: var(--text);
}

.icon-btn svg {
    display: block;
    width: 19px;
    height: 19px;
}

.icon-btn.theme-btn svg {
    width: 20px;
    height: 20px;
}

.login-card {
    width: 100%;
    max-width: 400px;
}

.login-card h2 {
    margin: 0;
    color: var(--text);
    font-size: 32px;
    letter-spacing: -0.5px;
}

.login-subtitle {
    margin: 10px 0 28px;
    color: var(--text-muted);
    font-size: 14px;
}

.login-btn {
    width: 100%;
    height: 46px;
    margin-top: 8px;
    font-size: 15px;
}

:deep(.el-input__wrapper) {
    height: 46px;
}

:deep(.el-form-item) {
    margin-bottom: 20px;
}

@media (max-width: 980px) {
    .login-left {
        display: none;
    }

    .login-right {
        flex: 1 1 100%;
        padding: 28px;
    }
}
</style>
