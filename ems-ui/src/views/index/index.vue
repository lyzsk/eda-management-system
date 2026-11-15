<template>
    <div class="layout">
        <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
            <div class="logo" @click="router.push('/dashboard')">
                <img src="/apple-touch-icon.png" alt="eda mangement system" />
                <span v-if="!sidebarCollapsed">{{ t("app.name") }}</span>
            </div>
            <el-scrollbar class="sidebar-scroll">
                <el-menu
                    :default-active="route.path"
                    router
                    class="menu"
                    :collapse="sidebarCollapsed"
                    :collapse-transition="false"
                >
                    <el-menu-item index="/dashboard">
                        <el-icon><HomeFilled /></el-icon>
                        <template #title>
                            <span>{{ t("menu.dashboard") }}</span>
                        </template>
                    </el-menu-item>

                    <el-sub-menu index="system">
                        <template #title>
                            <el-icon><Setting /></el-icon>
                            <span>{{ t("menu.system") }}</span>
                        </template>
                        <el-menu-item index="/system/user">
                            <el-icon><UserIcon /></el-icon>
                            <span>{{ t("menu.user") }}</span>
                        </el-menu-item>
                        <el-menu-item index="/system/role">
                            <el-icon><Avatar /></el-icon>
                            <span>{{ t("menu.role") }}</span>
                        </el-menu-item>
                        <el-menu-item index="/system/menu">
                            <el-icon><MenuIcon /></el-icon>
                            <span>{{ t("menu.menu") }}</span>
                        </el-menu-item>
                        <el-menu-item index="/system/dept">
                            <el-icon><OfficeBuilding /></el-icon>
                            <span>{{ t("menu.dept") }}</span>
                        </el-menu-item>
                        <el-menu-item index="/system/dict">
                            <el-icon><Collection /></el-icon>
                            <span>{{ t("menu.dict") }}</span>
                        </el-menu-item>
                    </el-sub-menu>

                    <el-sub-menu index="eda">
                        <template #title>
                            <el-icon><Cpu /></el-icon>
                            <span>{{ t("menu.eda") }}</span>
                        </template>
                        <el-menu-item index="/eda/list">
                            <el-icon><List /></el-icon>
                            <span>{{ t("menu.edaList") }}</span>
                        </el-menu-item>
                        <el-menu-item index="/eda/server">
                            <el-icon><Monitor /></el-icon>
                            <span>{{ t("menu.edaServer") }}</span>
                        </el-menu-item>
                        <el-menu-item index="/eda/script">
                            <el-icon><Tickets /></el-icon>
                            <span>{{ t("menu.edaScript") }}</span>
                        </el-menu-item>
                    </el-sub-menu>
                </el-menu>
            </el-scrollbar>
        </aside>

        <div class="main">
            <header class="header">
                <div class="header-left">
                    <button
                        class="icon-btn"
                        :title="
                            sidebarCollapsed
                                ? t('header.expand')
                                : t('header.collapse')
                        "
                        @click="sidebarCollapsed = !sidebarCollapsed"
                    >
                        <el-icon>
                            <Expand v-if="sidebarCollapsed" />
                            <Fold v-else />
                        </el-icon>
                    </button>
                    <nav class="breadcrumb">
                        <span>{{ t("app.name") }}</span>
                        <span class="sep">/</span>
                        <span class="current">{{ currentTitle }}</span>
                    </nav>
                </div>

                <div class="actions">
                    <button
                        class="icon-btn"
                        :title="
                            isFullscreen
                                ? t('header.exitFullscreen')
                                : t('header.fullscreen')
                        "
                        @click="toggleFullscreen"
                    >
                        <svg viewBox="0 0 24 24" aria-hidden="true">
                            <path
                                v-if="isFullscreen"
                                fill="currentColor"
                                d="M18 7h4v2h-6V3h2zM8 9H2V7h4V3h2zm10 8v4h-2v-6h6v2zM8 15v6H6v-4H2v-2z"
                            />
                            <path
                                v-else
                                fill="currentColor"
                                d="M8 3v2H4v4H2V3zM2 21v-6h2v4h4v2zm20 0h-6v-2h4v-4h2zm0-12h-2V5h-4V3h6z"
                            />
                        </svg>
                    </button>
                    <button
                        class="icon-btn"
                        :title="t('header.language')"
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
                            appStore.isDark
                                ? t('header.light')
                                : t('header.dark')
                        "
                        @click="toggleTheme"
                    >
                        <svg viewBox="0 0 24 24" aria-hidden="true">
                            <path
                                v-if="appStore.isDark"
                                fill="currentColor"
                                d="M12 18a6 6 0 1 1 0-12a6 6 0 0 1 0 12m0-2a4 4 0 1 0 0-8a4 4 0 0 0 0 8M11 1h2v3h-2zm0 19h2v3h-2zM3.515 4.929l1.414-1.414L7.05 5.636L5.636 7.05zM16.95 18.364l1.414-1.414l2.121 2.121l-1.414 1.414zm2.121-14.85l1.414 1.415l-2.121 2.121l-1.414-1.414zM5.636 16.95l1.414 1.414l-2.121 2.121l-1.414-1.414zM23 11v2h-3v-2zM4 11v2H1v-2z"
                            />
                            <path
                                v-else
                                fill="currentColor"
                                d="M10 7a7 7 0 0 0 12 4.9v.1c0 5.523-4.477 10-10 10S2 17.523 2 12S6.477 2 12 2h.1A6.98 6.98 0 0 0 10 7m-6 5a8 8 0 0 0 15.062 3.762A9 9 0 0 1 8.238 4.938A8 8 0 0 0 4 12"
                            />
                        </svg>
                    </button>
                    <UserMenu />
                </div>
            </header>

            <main class="content">
                <router-view />
            </main>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import {
    Avatar,
    Collection,
    Cpu,
    Expand,
    Fold,
    HomeFilled,
    List,
    Menu as MenuIcon,
    Monitor,
    OfficeBuilding,
    Setting,
    Tickets,
    User as UserIcon,
} from "@element-plus/icons-vue";
import UserMenu from "@/components/core/layouts/user-menu/index.vue";
import { useAppStore } from "@/store/modules/app";
import { toggleThemeWithAnimation } from "@/utils/themeAnimation";

const { t } = useI18n();
const appStore = useAppStore();
const route = useRoute();
const router = useRouter();

const sidebarCollapsed = ref(false);
const isFullscreen = ref(false);

const currentTitle = computed(() =>
    route.meta.title ? t(String(route.meta.title)) : t("menu.dashboard"),
);

function syncFullscreen() {
    isFullscreen.value = !!document.fullscreenElement;
}

async function toggleFullscreen() {
    try {
        if (document.fullscreenElement) {
            await document.exitFullscreen();
        } else {
            await document.documentElement.requestFullscreen();
        }
    } catch {
        // ignore browser restrictions
    }
}

function toggleLocale() {
    appStore.setLocale(appStore.locale === "zh-CN" ? "en" : "zh-CN");
}

function toggleTheme(event: MouseEvent) {
    toggleThemeWithAnimation(event, () => appStore.toggleTheme());
}

onMounted(() => {
    document.addEventListener("fullscreenchange", syncFullscreen);
});

onUnmounted(() => {
    document.removeEventListener("fullscreenchange", syncFullscreen);
});
</script>

<style scoped lang="scss">
.layout {
    display: flex;
    height: 100vh;
    overflow: hidden;
    background: var(--app-bg);
    color: var(--text);
}

.sidebar {
    display: flex;
    flex-shrink: 0;
    flex-direction: column;
    width: 220px;
    overflow: hidden;
    border-right: 1px solid var(--border);
    background: var(--sidebar-bg);
    transition: width 0.25s;
    user-select: none;
}

.sidebar.collapsed {
    width: 64px;
}

.logo {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    height: 60px;
    padding: 0 16px;
    overflow: hidden;
    border-bottom: 1px solid var(--border);
    cursor: pointer;
}

.logo img {
    width: 32px;
    height: 32px;
    object-fit: contain;
    border-radius: 8px;
    background: #fff;
}

.logo span {
    margin-left: 10px;
    overflow: hidden;
    color: var(--text);
    font-size: 15px;
    font-weight: 600;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.sidebar.collapsed .logo {
    justify-content: center;
    padding: 0;
}

.sidebar.collapsed .logo span {
    display: none;
}

.sidebar-scroll {
    height: calc(100% - 60px);
}

.menu {
    border-right: 0;
    background: transparent;
}

:deep(.el-menu) {
    border-right: 0;
    background: transparent;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
    height: 42px;
    margin: 0 8px 4px;
    border-radius: 8px;
    line-height: 42px;
}

:deep(.el-menu-item.is-active) {
    background: rgb(93 135 255 / 12%);
    color: var(--primary);
}

:deep(.el-sub-menu .el-menu-item) {
    min-width: 0;
}

.main {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-width: 0;
}

.header {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: space-between;
    height: 60px;
    padding: 0 20px;
    border-bottom: 1px solid var(--border);
    background: var(--panel-bg);
}

.header-left {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
}

.breadcrumb {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--text-muted);
    font-size: 13px;
    white-space: nowrap;
}

.breadcrumb .current {
    color: var(--text);
    font-weight: 500;
}

.breadcrumb .sep {
    color: var(--text-muted);
}

.actions {
    display: flex;
    align-items: center;
    gap: 4px;
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

.content {
    flex: 1;
    padding: 20px;
    overflow: auto;
}
</style>
