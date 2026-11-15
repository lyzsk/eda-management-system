<template>
    <div class="page-card">
        <div class="page-header">
            <div class="page-header-text">
                <h1>脚本管理</h1>
                <p>
                    按 EDA 工具登记启动/停止/重启/状态/任务脚本，异步 SSH
                    执行并保留历史
                </p>
            </div>
            <div class="page-header-actions">
                <el-button
                    type="primary"
                    :icon="Plus"
                    :disabled="!toolId"
                    @click="openCreate"
                >
                    新增脚本
                </el-button>
            </div>
        </div>

        <div class="toolbar">
            <el-select
                v-model="toolId"
                placeholder="选择 EDA 工具"
                filterable
                style="width: 340px"
                @change="reloadAll"
            >
                <el-option
                    v-for="tool in tools"
                    :key="tool.id"
                    :label="toolLabel(tool)"
                    :value="tool.id"
                />
            </el-select>
            <el-button :icon="Refresh" @click="reloadAll">刷新</el-button>
        </div>

        <el-empty v-if="!toolId" description="请选择 EDA 工具后管理脚本" />

        <template v-else>
            <el-table v-loading="loading" :data="scripts">
                <el-table-column label="动作" width="90">
                    <template #default="{ row }">
                        <el-tag :type="actionTag(row.action)">
                            {{ actionLabel(row.action) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="scriptName" label="名称" min-width="150" />
                <el-table-column prop="execMode" label="方式" width="110" />
                <el-table-column
                    prop="command"
                    label="命令"
                    min-width="260"
                    show-overflow-tooltip
                />
                <el-table-column
                    prop="workdir"
                    label="工作目录"
                    min-width="140"
                    show-overflow-tooltip
                />
                <el-table-column prop="timeoutSeconds" label="超时(s)" width="90" />
                <el-table-column label="启用" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.enabled === 1 ? 'success' : 'info'">
                            {{ row.enabled === 1 ? "启用" : "停用" }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            link
                            type="primary"
                            :disabled="row.enabled !== 1"
                            @click="execute(row)"
                        >
                            执行
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

            <div class="section-title">执行历史（最近 50 条）</div>
            <el-table v-loading="runsLoading" :data="runs">
                <el-table-column label="开始时间" width="170">
                    <template #default="{ row }">
                        {{ formatTime(row.startedAt || row.createTime) }}
                    </template>
                </el-table-column>
                <el-table-column label="动作" width="90">
                    <template #default="{ row }">
                        <el-tag :type="actionTag(row.action)">
                            {{ actionLabel(row.action) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="statusTag(row.status)">
                            {{ statusLabel(row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="exitCode" label="退出码" width="90" />
                <el-table-column prop="operatorName" label="操作人" width="120" />
                <el-table-column prop="sshHost" label="主机" width="150" />
                <el-table-column label="耗时" width="90">
                    <template #default="{ row }">{{ duration(row) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="110" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="showRun(row)">
                            查看输出
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </template>

        <el-dialog
            v-model="dialogVisible"
            :title="editingId ? '编辑脚本' : '新增脚本'"
            width="680px"
        >
            <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="动作" prop="action">
                            <el-select v-model="form.action" style="width: 100%">
                                <el-option label="启动 START" value="START" />
                                <el-option label="停止 STOP" value="STOP" />
                                <el-option label="重启 RESTART" value="RESTART" />
                                <el-option label="状态 STATUS" value="STATUS" />
                                <el-option label="任务 TASK" value="TASK" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="名称" prop="scriptName">
                            <el-input
                                v-model="form.scriptName"
                                placeholder="如 启动 Virtuoso"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="执行方式">
                            <el-select v-model="form.execMode" style="width: 100%">
                                <el-option label="SHELL 命令" value="SHELL" />
                                <el-option label="SCRIPT 脚本路径" value="SCRIPT" />
                                <el-option label="POWERSHELL" value="POWERSHELL" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="超时(秒)">
                            <el-input-number
                                v-model="form.timeoutSeconds"
                                :min="1"
                                :max="3600"
                                style="width: 100%"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="命令" prop="command">
                            <el-input
                                v-model="form.command"
                                type="textarea"
                                :rows="3"
                                placeholder="登记好的完整命令，如 /opt/eda/start_virtuoso.sh --lic 27000@lic01"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="工作目录">
                            <el-input v-model="form.workdir" placeholder="可空" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="排序">
                            <el-input-number
                                v-model="form.sortNo"
                                :min="0"
                                :max="9999"
                                style="width: 100%"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="启用">
                            <el-switch
                                v-model="form.enabled"
                                :active-value="1"
                                :inactive-value="0"
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

        <el-dialog v-model="resultVisible" :title="resultTitle" width="760px">
            <div v-if="resultRun" class="result-meta">
                <el-tag :type="statusTag(resultRun.status)">
                    {{ statusLabel(resultRun.status) }}
                </el-tag>
                <span>退出码：{{ resultRun.exitCode ?? "-" }}</span>
                <span>耗时：{{ duration(resultRun) }}</span>
                <span>操作人：{{ resultRun.operatorName || "-" }}</span>
            </div>
            <pre class="console">{{
                resultRun?.output || resultRun?.errorMsg || "(无输出)"
            }}</pre>
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
import { Plus, Refresh } from "@element-plus/icons-vue";
import {
    createScript,
    deleteScript,
    getScriptRun,
    listEdaTools,
    listScriptRuns,
    listScripts,
    runScript,
    updateScript,
} from "@/api/eda";
import type { EdaScript, EdaScriptRun, EdaTool } from "@/types/system";

const ACTION_LABELS: Record<string, string> = {
    START: "启动",
    STOP: "停止",
    RESTART: "重启",
    STATUS: "状态",
    TASK: "任务",
};

const STATUS_LABELS: Record<string, string> = {
    PENDING: "排队中",
    RUNNING: "执行中",
    SUCCESS: "成功",
    FAILED: "失败",
    TIMEOUT: "超时",
};

const tools = ref<EdaTool[]>([]);
const toolId = ref("");
const scripts = ref<EdaScript[]>([]);
const runs = ref<EdaScriptRun[]>([]);
const loading = ref(false);
const runsLoading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const editingId = ref("");
const formRef = ref<FormInstance>();
const resultVisible = ref(false);
const resultRun = ref<EdaScriptRun | null>(null);

const emptyForm = (): Partial<EdaScript> => ({
    toolId: toolId.value,
    action: "START",
    scriptName: "",
    execMode: "SHELL",
    command: "",
    workdir: "",
    timeoutSeconds: 60,
    enabled: 1,
    sortNo: 0,
    remark: "",
});

const form = reactive<Partial<EdaScript>>(emptyForm());

const rules: FormRules = {
    action: [{ required: true, message: "请选择动作", trigger: "change" }],
    scriptName: [{ required: true, message: "请输入脚本名称", trigger: "blur" }],
    command: [{ required: true, message: "请输入命令", trigger: "blur" }],
};

const resultTitle = ref("执行结果");

function toolLabel(tool: EdaTool) {
    return tool.host ? `${tool.toolName}（${tool.host}）` : tool.toolName;
}

function actionLabel(action?: string) {
    return action ? (ACTION_LABELS[action] ?? action) : "-";
}

function actionTag(action?: string) {
    if (action === "START") {
        return "success";
    }
    if (action === "STOP") {
        return "danger";
    }
    if (action === "RESTART") {
        return "warning";
    }
    return "info";
}

function statusLabel(status?: string) {
    return status ? (STATUS_LABELS[status] ?? status) : "-";
}

function statusTag(status?: string) {
    if (status === "SUCCESS") {
        return "success";
    }
    if (status === "FAILED") {
        return "danger";
    }
    if (status === "TIMEOUT") {
        return "warning";
    }
    return "info";
}

function formatTime(value?: string) {
    return value ? value.replace("T", " ").slice(0, 19) : "-";
}

function toDate(value?: string) {
    return value ? new Date(value.replace(" ", "T")) : null;
}

function duration(row: EdaScriptRun) {
    const start = toDate(row.startedAt);
    const end = toDate(row.finishedAt);
    if (!start || !end) {
        return "-";
    }
    const ms = end.getTime() - start.getTime();
    return ms < 1000 ? `${ms}ms` : `${(ms / 1000).toFixed(1)}s`;
}

async function loadTools() {
    tools.value = await listEdaTools();
    if (!toolId.value && tools.value.length) {
        toolId.value = tools.value[0].id;
    }
}

async function loadScripts() {
    if (!toolId.value) {
        return;
    }
    loading.value = true;
    try {
        scripts.value = await listScripts(toolId.value);
    } finally {
        loading.value = false;
    }
}

async function loadRuns() {
    if (!toolId.value) {
        return;
    }
    runsLoading.value = true;
    try {
        runs.value = await listScriptRuns(toolId.value, 50);
    } finally {
        runsLoading.value = false;
    }
}

async function reloadAll() {
    await Promise.all([loadScripts(), loadRuns()]);
}

function openCreate() {
    editingId.value = "";
    Object.assign(form, emptyForm());
    dialogVisible.value = true;
    nextTick(() => formRef.value?.clearValidate());
}

function openEdit(row: EdaScript) {
    editingId.value = row.id;
    Object.assign(form, {
        toolId: row.toolId,
        action: row.action,
        scriptName: row.scriptName,
        execMode: row.execMode ?? "SHELL",
        command: row.command,
        workdir: row.workdir ?? "",
        timeoutSeconds: row.timeoutSeconds ?? 60,
        enabled: row.enabled,
        sortNo: row.sortNo ?? 0,
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
        const payload: Partial<EdaScript> = { ...form, toolId: toolId.value };
        if (editingId.value) {
            await updateScript(editingId.value, payload);
        } else {
            await createScript(payload);
        }
        ElMessage.success("保存成功");
        dialogVisible.value = false;
        await loadScripts();
    } finally {
        saving.value = false;
    }
}

async function remove(row: EdaScript) {
    const confirmed = await ElMessageBox.confirm(
        `确认删除脚本「${row.scriptName}」吗？`,
        "提示",
        { type: "warning" },
    )
        .then(() => true)
        .catch(() => false);
    if (!confirmed) {
        return;
    }
    await deleteScript(row.id);
    ElMessage.success("删除成功");
    await loadScripts();
}

async function pollRun(runId: string): Promise<EdaScriptRun> {
    const deadline = Date.now() + 5 * 60 * 1000;
    while (Date.now() < deadline) {
        const run = await getScriptRun(runId);
        if (run.status !== "PENDING" && run.status !== "RUNNING") {
            return run;
        }
        await new Promise((resolve) => setTimeout(resolve, 1500));
    }
    throw new Error("等待执行结果超时（5 分钟）");
}

async function execute(row: EdaScript) {
    const confirmed = await ElMessageBox.confirm(
        `确认在服务器上执行「${row.scriptName}」？\n命令：${row.command}`,
        "执行确认",
        { type: "warning", confirmButtonText: "执行", cancelButtonText: "取消" },
    )
        .then(() => true)
        .catch(() => false);
    if (!confirmed) {
        return;
    }
    try {
        const runId = await runScript(row.id);
        ElMessage.info("已提交执行，等待结果…");
        const run = await pollRun(runId);
        resultRun.value = run;
        resultTitle.value = `执行结果 · ${actionLabel(run.action)}`;
        resultVisible.value = true;
        await loadRuns();
    } catch (error) {
        ElMessage.error(error instanceof Error ? error.message : "执行失败");
    }
}

function showRun(row: EdaScriptRun) {
    resultRun.value = row;
    resultTitle.value = `执行结果 · ${actionLabel(row.action)}`;
    resultVisible.value = true;
}

onMounted(async () => {
    await loadTools();
    if (toolId.value) {
        await reloadAll();
    }
});
</script>

<style scoped lang="scss">
.section-title {
    margin: 20px 0 10px;
    color: var(--text);
    font-size: 14px;
    font-weight: 600;
}

.result-meta {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 10px;
    color: var(--text-muted);
    font-size: 13px;
}

.console {
    max-height: 420px;
    margin: 0;
    padding: 12px;
    overflow: auto;
    border-radius: 8px;
    background: #0f1115;
    color: #d5d9e0;
    font-family: Consolas, Menlo, monospace;
    font-size: 12px;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-all;
}
</style>
