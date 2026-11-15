<template>
    <div class="dashboard">
        <div class="page-header">
            <div class="page-header-text">
                <h1>
                    {{ t("dashboard.welcome") }},
                    {{ userStore.user?.realName || userStore.user?.username }}
                </h1>
                <p>{{ t("dashboard.welcomeDesc") }}</p>
            </div>
        </div>

        <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :lg="6">
                <el-card class="stat-card">
                    <div class="stat-label">{{ t("dashboard.users") }}</div>
                    <div class="stat-value">{{ users.length }}</div>
                </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
                <el-card class="stat-card">
                    <div class="stat-label">{{ t("dashboard.roles") }}</div>
                    <div class="stat-value">{{ roles.length }}</div>
                </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
                <el-card class="stat-card">
                    <div class="stat-label">{{ t("dashboard.tools") }}</div>
                    <div class="stat-value">{{ tools.length }}</div>
                </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
                <el-card class="stat-card">
                    <div class="stat-label">{{ t("dashboard.online") }}</div>
                    <div class="stat-value green">{{ onlineCount }}</div>
                </el-card>
            </el-col>
        </el-row>

        <div class="table-card tool-card">
            <div class="card-title">EDA 工具状态</div>
            <el-table :data="tools" v-loading="loading">
                <el-table-column prop="toolName" label="工具名称" min-width="180" />
                <el-table-column prop="host" label="主机" min-width="140" />
                <el-table-column prop="osType" label="系统" width="110" />
                <el-table-column prop="toolVersion" label="版本" width="130" />
                <el-table-column prop="licenseStatus" label="License" width="120" />
                <el-table-column label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'info'">
                            {{ row.status === 1 ? "在线" : "离线" }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="最后心跳" width="180">
                    <template #default="{ row }">
                        {{ formatTime(row.lastHeartbeat) }}
                    </template>
                </el-table-column>
            </el-table>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { listUsers } from "@/api/system/user";
import { listRoles } from "@/api/system/role";
import { listEdaTools } from "@/api/eda";
import { useUserStore } from "@/store/modules/user";
import type { EdaTool, SysRole, SysUser } from "@/types/system";

const { t } = useI18n();
const userStore = useUserStore();

const users = ref<SysUser[]>([]);
const roles = ref<SysRole[]>([]);
const tools = ref<EdaTool[]>([]);
const loading = ref(false);

const onlineCount = computed(
    () => tools.value.filter((tool) => tool.status === 1).length,
);

function formatTime(value?: string) {
    return value ? value.replace("T", " ").slice(0, 19) : "-";
}

async function load() {
    loading.value = true;
    const [userResult, roleResult, toolResult] = await Promise.allSettled([
        listUsers(),
        listRoles(),
        listEdaTools(),
    ]);
    if (userResult.status === "fulfilled") {
        users.value = userResult.value;
    }
    if (roleResult.status === "fulfilled") {
        roles.value = roleResult.value;
    }
    if (toolResult.status === "fulfilled") {
        tools.value = toolResult.value;
    }
    loading.value = false;
}

onMounted(load);
</script>

<style scoped lang="scss">
.dashboard {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.stat-card {
    border-color: var(--border);
    background: var(--panel-bg);
    color: var(--text);
}

.stat-label {
    color: var(--text-muted);
    font-size: 13px;
}

.stat-value {
    margin-top: 10px;
    font-size: 28px;
    font-weight: 700;
}

.stat-value.green {
    color: #16a34a;
}

.tool-card {
    margin-top: 16px;
}

.card-title {
    margin-bottom: 14px;
    font-size: 15px;
    font-weight: 600;
}

:deep(.el-card) {
    border-color: var(--border);
    background: var(--panel-bg);
    color: var(--text);
}
</style>
