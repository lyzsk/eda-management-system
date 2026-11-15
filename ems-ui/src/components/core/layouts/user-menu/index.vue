<template>
    <el-popover
        ref="popoverRef"
        placement="bottom-end"
        :width="240"
        :hide-after="0"
        :offset="10"
        trigger="hover"
        :show-arrow="false"
        popper-class="user-menu-popover"
        popper-style="padding: 5px 16px"
    >
        <template #reference>
            <button class="user-trigger" aria-label="用户菜单">
                <img class="user-avatar" :src="avatar" alt="用户头像" />
            </button>
        </template>
        <div class="user-card">
            <div class="user-summary">
                <img class="summary-avatar" :src="avatar" alt="" />
                <div class="summary-text">
                    <span class="summary-name">{{ username }}</span>
                    <span class="summary-role">{{ roleName }}</span>
                </div>
            </div>
            <button class="logout-button" @click="confirmLogout">
                {{ t("menu.logout") }}
            </button>
        </div>
    </el-popover>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { ElMessageBox, ElPopover } from "element-plus";
import defaultAvatar from "@/assets/images/avatars/avatar-default.png";
import { logout as logoutApi } from "@/api/auth";
import { useUserStore } from "@/store/modules/user";

const router = useRouter();
const { t } = useI18n();
const userStore = useUserStore();
const popoverRef = ref<InstanceType<typeof ElPopover>>();

const avatar = computed(() => userStore.user?.avatar || defaultAvatar);
const username = computed(
    () => userStore.user?.realName || userStore.user?.username || "-",
);
const roleName = computed(() => userStore.user?.roleCode || "USER");

async function confirmLogout() {
    popoverRef.value?.hide();
    await ElMessageBox.confirm("确认退出当前账号吗？", "提示", {
        confirmButtonText: "退出登录",
        cancelButtonText: "取消",
        type: "warning",
    });
    try {
        await logoutApi();
    } catch {
        // ignore logout API failures
    }
    userStore.logout();
    router.replace("/login");
}
</script>

<style scoped lang="scss">
.user-trigger {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    padding: 0;
    margin-left: 6px;
    border: 0;
    border-radius: 50%;
    background: transparent;
    cursor: pointer;
}

.user-avatar {
    width: 34px;
    height: 34px;
    object-fit: cover;
    border-radius: 50%;
    transition: box-shadow 0.2s;
}

.user-trigger:hover .user-avatar {
    box-shadow: 0 0 0 3px var(--slider-bg);
}

.user-card {
    padding-top: 12px;
}

.user-summary {
    display: flex;
    align-items: center;
    padding: 0 0 12px;
    border-bottom: 1px solid var(--border);
}

.summary-avatar {
    width: 40px;
    height: 40px;
    margin-right: 12px;
    object-fit: cover;
    border-radius: 50%;
}

.summary-text {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-width: 0;
}

.summary-name {
    overflow: hidden;
    color: var(--text);
    font-size: 14px;
    font-weight: 500;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.summary-role {
    overflow: hidden;
    margin-top: 3px;
    color: var(--text-muted);
    font-size: 12px;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.logout-button {
    width: 100%;
    padding: 7px 0;
    margin: 12px 0 10px;
    border: 1px solid var(--border);
    border-radius: 6px;
    background: transparent;
    color: var(--text);
    font-size: 12px;
    cursor: pointer;
    transition: box-shadow 0.2s;
}

.logout-button:hover {
    box-shadow: 0 5px 16px rgb(0 0 0 / 10%);
}
</style>
