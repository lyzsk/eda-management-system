<template>
    <div class="page-card">
        <div class="page-header">
            <div class="page-header-text">
                <h1>EDA列表</h1>
                <p>登记与查看部署在服务器上的 EDA 工具</p>
            </div>
            <div class="page-header-actions">
                <el-button type="primary" :icon="Plus" @click="openCreate">
                    新增工具
                </el-button>
            </div>
        </div>

        <div class="toolbar">
            <el-input
                v-model="keyword"
                placeholder="工具名称 / 主机"
                clearable
                style="width: 240px"
                @keyup.enter="load"
            />
            <el-button type="primary" :icon="Search" @click="load">
                搜索
            </el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
        </div>

        <el-table v-loading="loading" :data="tools">
            <el-table-column prop="toolName" label="工具名称" min-width="180" />
            <el-table-column prop="toolType" label="类型" width="110" />
            <el-table-column prop="host" label="主机" min-width="130" />
            <el-table-column prop="osType" label="系统" width="100" />
            <el-table-column prop="toolVersion" label="版本" width="120" />
            <el-table-column label="License" width="100">
                <template #default="{ row }">
                    <el-tag :type="licenseTag(row.licenseStatus)">
                        {{ licenseLabel(row.licenseStatus) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
                <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'info'">
                        {{ row.status === 1 ? "在线" : "离线" }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="最后心跳" width="170">
                <template #default="{ row }">
                    {{ formatTime(row.lastHeartbeat) }}
                </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
                <template #default="{ row }">
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
            :title="editingId ? '编辑 EDA 工具' : '新增 EDA 工具'"
            width="620px"
        >
            <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="工具名称" prop="toolName">
                            <el-input
                                v-model="form.toolName"
                                placeholder="如 Cadence Virtuoso"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="工具类型">
                            <el-input
                                v-model="form.toolType"
                                placeholder="如 模拟设计"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="主机">
                            <el-input v-model="form.host" placeholder="eda-linux-01" />
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
                        <el-form-item label="版本">
                            <el-input v-model="form.toolVersion" placeholder="IC618" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="License">
                            <el-select
                                v-model="form.licenseStatus"
                                style="width: 100%"
                            >
                                <el-option label="有效" value="VALID" />
                                <el-option label="已过期" value="EXPIRED" />
                                <el-option label="未知" value="UNKNOWN" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="状态">
                            <el-radio-group v-model="form.status">
                                <el-radio :value="1">在线</el-radio>
                                <el-radio :value="0">离线</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="最后心跳">
                            <el-date-picker
                                v-model="form.lastHeartbeat"
                                type="datetime"
                                value-format="YYYY-MM-DDTHH:mm:ss"
                                placeholder="选择时间"
                                style="width: 100%"
                            />
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
    createEdaTool,
    deleteEdaTool,
    listEdaTools,
    updateEdaTool,
} from "@/api/eda";
import type { EdaTool } from "@/types/system";

const tools = ref<EdaTool[]>([]);
const keyword = ref("");
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const editingId = ref("");
const formRef = ref<FormInstance>();

const form = reactive<Partial<EdaTool>>({
    toolName: "",
    toolType: "",
    host: "",
    osType: "Linux",
    toolVersion: "",
    licenseStatus: "UNKNOWN",
    status: 1,
    lastHeartbeat: "",
    remark: "",
});

const rules: FormRules = {
    toolName: [{ required: true, message: "请输入工具名称", trigger: "blur" }],
};

function formatTime(value?: string) {
    return value ? value.replace("T", " ").slice(0, 19) : "-";
}

function licenseLabel(value?: string) {
    return value === "VALID" ? "有效" : value === "EXPIRED" ? "已过期" : "未知";
}

function licenseTag(value?: string) {
    return value === "VALID"
        ? "success"
        : value === "EXPIRED"
          ? "danger"
          : "info";
}

async function load() {
    loading.value = true;
    try {
        tools.value = await listEdaTools(keyword.value || undefined);
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
    Object.assign(form, {
        toolName: "",
        toolType: "",
        host: "",
        osType: "Linux",
        toolVersion: "",
        licenseStatus: "UNKNOWN",
        status: 1,
        lastHeartbeat: "",
        remark: "",
    });
    dialogVisible.value = true;
    nextTick(() => formRef.value?.clearValidate());
}

function openEdit(row: EdaTool) {
    editingId.value = row.id;
    Object.assign(form, {
        toolName: row.toolName,
        toolType: row.toolType ?? "",
        host: row.host ?? "",
        osType: row.osType ?? "Linux",
        toolVersion: row.toolVersion ?? "",
        licenseStatus: row.licenseStatus ?? "UNKNOWN",
        status: row.status,
        lastHeartbeat: row.lastHeartbeat ?? "",
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
        const payload = { ...form };
        if (!payload.lastHeartbeat) {
            delete payload.lastHeartbeat;
        }
        if (editingId.value) {
            await updateEdaTool(editingId.value, payload);
        } else {
            await createEdaTool(payload);
        }
        ElMessage.success("保存成功");
        dialogVisible.value = false;
        await load();
    } finally {
        saving.value = false;
    }
}

async function remove(row: EdaTool) {
    const confirmed = await ElMessageBox.confirm(
        `确认删除 EDA 工具「${row.toolName}」吗？`,
        "提示",
        { type: "warning" },
    )
        .then(() => true)
        .catch(() => false);
    if (!confirmed) {
        return;
    }
    await deleteEdaTool(row.id);
    ElMessage.success("删除成功");
    await load();
}

onMounted(load);
</script>
