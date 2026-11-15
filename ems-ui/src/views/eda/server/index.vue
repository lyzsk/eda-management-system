<template>
    <div class="page-card">
        <div class="page-header">
            <div class="page-header-text">
                <h1>服务器管理</h1>
                <p>
                    配置 SSH 执行目标；私钥由部署配置（EMS_SSH_KEY_FILE /
                    EMS_SSH_PRIVATE_KEY）统一提供，不经过页面与数据库
                </p>
            </div>
            <div class="page-header-actions">
                <el-button type="primary" :icon="Plus" @click="openCreate">
                    新增服务器
                </el-button>
            </div>
        </div>

        <div class="toolbar">
            <el-input
                v-model="keyword"
                placeholder="编码 / 名称 / 主机"
                clearable
                style="width: 240px"
                @keyup.enter="load"
            />
            <el-button type="primary" :icon="Search" @click="load">
                搜索
            </el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
        </div>

        <el-table v-loading="loading" :data="servers">
            <el-table-column prop="serverCode" label="编码" width="160" />
            <el-table-column prop="serverName" label="名称" min-width="140" />
            <el-table-column label="主机" min-width="180">
                <template #default="{ row }">
                    {{ row.host }}:{{ row.sshPort }}
                </template>
            </el-table-column>
            <el-table-column prop="osType" label="系统" width="100" />
            <el-table-column label="SSH 账号" width="140">
                <template #default="{ row }">
                    {{ row.username || "默认账号" }}
                </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
                <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'info'">
                        {{ row.status === 1 ? "启用" : "停用" }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                    <el-button
                        link
                        type="primary"
                        :loading="testingId === row.id"
                        @click="testConnection(row)"
                    >
                        测试连接
                    </el-button>
                    <el-button link type="primary" @click="openEdit(row)">
                        编辑
                    </el-button>
                    <el-button link type="danger" @click="remove(row)">
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-dialog
            v-model="dialogVisible"
            :title="editingId ? '编辑服务器' : '新增服务器'"
            width="640px"
        >
            <el-alert
                type="info"
                :closable="false"
                show-icon
                title="认证使用全局部署密钥；如需不同账号，仅填写 SSH 账号即可"
                style="margin-bottom: 16px"
            />
            <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="服务器编码" prop="serverCode">
                            <el-input
                                v-model="form.serverCode"
                                placeholder="如 eda-linux-01"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="名称">
                            <el-input
                                v-model="form.serverName"
                                placeholder="如 Linux 计算节点 01"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="主机" prop="host">
                            <el-input v-model="form.host" placeholder="IP 或域名" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="SSH 端口">
                            <el-input-number
                                v-model="form.sshPort"
                                :min="1"
                                :max="65535"
                                style="width: 100%"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="操作系统">
                            <el-select v-model="form.osType" style="width: 100%">
                                <el-option label="Linux" value="Linux" />
                                <el-option label="Windows" value="Windows" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="SSH 账号">
                            <el-input
                                v-model="form.username"
                                placeholder="留空使用全局默认账号"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="连接超时">
                            <el-input-number
                                v-model="form.connectTimeoutSeconds"
                                :min="1"
                                :max="120"
                                style="width: 100%"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="状态">
                            <el-radio-group v-model="form.status">
                                <el-radio :value="1">启用</el-radio>
                                <el-radio :value="0">停用</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="备注">
                            <el-input v-model="form.remark" type="textarea" :rows="2" />
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" :loading="saving" @click="save">
                        确定
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from "vue";
import {
    ElMessage,
    ElMessageBox,
    type FormInstance,
    type FormRules,
} from "element-plus";
import { Plus, Refresh, Search } from "@element-plus/icons-vue";
import {
    createServer,
    deleteServer,
    listServers,
    testServer,
    updateServer,
} from "@/api/eda";
import type { EdaServer, EdaServerForm } from "@/types/system";

const servers = ref<EdaServer[]>([]);
const keyword = ref("");
const loading = ref(false);
const saving = ref(false);
const testingId = ref("");
const dialogVisible = ref(false);
const editingId = ref("");
const formRef = ref<FormInstance>();

const emptyForm = (): EdaServerForm => ({
    serverCode: "",
    serverName: "",
    host: "",
    sshPort: 22,
    osType: "Linux",
    username: "",
    connectTimeoutSeconds: 10,
    status: 1,
    remark: "",
});

const form = reactive<EdaServerForm>(emptyForm());

const rules: FormRules = {
    serverCode: [{ required: true, message: "请输入服务器编码", trigger: "blur" }],
    host: [{ required: true, message: "请输入主机", trigger: "blur" }],
};

async function load() {
    loading.value = true;
    try {
        servers.value = await listServers(keyword.value || undefined);
    } finally {
        loading.value = false;
    }
}

function reset() {
    keyword.value = "";
    load();
}

function openCreate() {
    editingId.value = "";
    Object.assign(form, emptyForm());
    dialogVisible.value = true;
    nextTick(() => formRef.value?.clearValidate());
}

function openEdit(row: EdaServer) {
    editingId.value = row.id;
    Object.assign(form, {
        serverCode: row.serverCode,
        serverName: row.serverName ?? "",
        host: row.host,
        sshPort: row.sshPort ?? 22,
        osType: row.osType ?? "Linux",
        username: row.username ?? "",
        connectTimeoutSeconds: row.connectTimeoutSeconds ?? 10,
        status: row.status,
        remark: row.remark ?? "",
    });
    dialogVisible.value = true;
    nextTick(() => formRef.value?.clearValidate());
}

async function save() {
    if (!formRef.value) {
        return;
    }
    const valid = await formRef.value.validate().catch(() => false);
    if (!valid) {
        return;
    }
    saving.value = true;
    try {
        const payload: EdaServerForm = { ...form };
        if (editingId.value) {
            await updateServer(editingId.value, payload);
        } else {
            await createServer(payload);
        }
        ElMessage.success("保存成功");
        dialogVisible.value = false;
        await load();
    } finally {
        saving.value = false;
    }
}

async function remove(row: EdaServer) {
    const confirmed = await ElMessageBox.confirm(
        `确认删除服务器「${row.serverCode}」吗？`,
        "提示",
        { type: "warning" },
    )
        .then(() => true)
        .catch(() => false);
    if (!confirmed) {
        return;
    }
    await deleteServer(row.id);
    ElMessage.success("删除成功");
    await load();
}

async function testConnection(row: EdaServer) {
    testingId.value = row.id;
    try {
        const result = await testServer(row.id);
        if (result.timedOut) {
            ElMessageBox.alert(result.errorMsg || "连接超时", "连接失败");
        } else if (result.exitCode === 0) {
            ElMessage.success(`连接成功：${result.output.trim()}`);
        } else {
            ElMessageBox.alert(
                `${result.errorMsg || "未知错误"}\n${result.output || ""}`,
                "连接失败",
            );
        }
    } catch {
        // 业务错误已由 http 拦截器提示（如未配置 SSH 私钥）
    } finally {
        testingId.value = "";
    }
}

onMounted(load);
</script>
