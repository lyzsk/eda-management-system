<template>
    <div class="page-card">
        <div class="page-header">
            <div class="page-header-text">
                <h1>字典管理</h1>
                <p>维护系统字典类型与字典数据</p>
            </div>
            <div class="page-header-actions">
                <el-button type="primary" :icon="Plus" @click="openCreateType">
                    新增字典
                </el-button>
            </div>
        </div>

        <div class="toolbar">
            <el-input
                v-model="keyword"
                placeholder="字典名称 / 类型"
                clearable
                style="width: 240px"
                @keyup.enter="loadTypes"
            />
            <el-button type="primary" :icon="Search" @click="loadTypes">
                搜索
            </el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
        </div>

        <el-table v-loading="loading" :data="types">
            <el-table-column prop="dictType" label="字典类型" min-width="180" />
            <el-table-column prop="dictName" label="字典名称" min-width="160" />
            <el-table-column label="状态" width="100">
                <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'info'">
                        {{ row.status === 1 ? "启用" : "停用" }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="160" />
            <el-table-column label="创建时间" width="180">
                <template #default="{ row }">
                    {{ formatTime(row.createTime) }}
                </template>
            </el-table-column>
            <el-table-column label="操作" width="230" fixed="right">
                <template #default="{ row }">
                    <el-button link type="primary" @click="openData(row)">
                        字典数据
                    </el-button>
                    <el-button link type="primary" @click="openEditType(row)">
                        编辑
                    </el-button>
                    <el-button link type="danger" @click="removeType(row)">
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-dialog
            v-model="typeDialogVisible"
            :title="editingTypeId ? '编辑字典' : '新增字典'"
            width="520px"
        >
            <el-form
                ref="typeFormRef"
                :model="typeForm"
                :rules="typeRules"
                label-width="90px"
            >
                <el-form-item label="字典类型" prop="dictType">
                    <el-input
                        v-model="typeForm.dictType"
                        placeholder="如 eda_os_type"
                    />
                </el-form-item>
                <el-form-item label="字典名称" prop="dictName">
                    <el-input v-model="typeForm.dictName" placeholder="如 操作系统" />
                </el-form-item>
                <el-form-item label="状态">
                    <el-radio-group v-model="typeForm.status">
                        <el-radio :value="1">启用</el-radio>
                        <el-radio :value="0">停用</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="typeForm.remark" type="textarea" :rows="2" />
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="typeDialogVisible = false">取消</el-button>
                    <el-button type="primary" :loading="saving" @click="saveType">
                        确定
                    </el-button>
                </div>
            </template>
        </el-dialog>

        <el-dialog
            v-model="dataDialogVisible"
            :title="`字典数据 - ${currentType?.dictName || ''}`"
            width="760px"
        >
            <div class="toolbar">
                <el-button type="primary" :icon="Plus" @click="openCreateData">
                    新增数据
                </el-button>
            </div>
            <el-table v-loading="dataLoading" :data="dictData">
                <el-table-column prop="label" label="显示文本" min-width="140" />
                <el-table-column prop="dictValue" label="字典值" min-width="140" />
                <el-table-column prop="sortNo" label="排序" width="80" />
                <el-table-column label="状态" width="90">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'info'">
                            {{ row.status === 1 ? "启用" : "停用" }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="remark" label="备注" min-width="120" />
                <el-table-column label="操作" width="140" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="openEditData(row)">
                            编辑
                        </el-button>
                        <el-button link type="danger" @click="removeData(row)">
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-dialog>

        <el-dialog
            v-model="dataFormVisible"
            :title="editingDataId ? '编辑字典数据' : '新增字典数据'"
            width="480px"
            append-to-body
        >
            <el-form
                ref="dataFormRef"
                :model="dataForm"
                :rules="dataRules"
                label-width="90px"
            >
                <el-form-item label="字典类型">
                    <el-input :model-value="currentType?.dictType" disabled />
                </el-form-item>
                <el-form-item label="显示文本" prop="label">
                    <el-input v-model="dataForm.label" />
                </el-form-item>
                <el-form-item label="字典值" prop="dictValue">
                    <el-input v-model="dataForm.dictValue" />
                </el-form-item>
                <el-form-item label="排序">
                    <el-input-number v-model="dataForm.sortNo" :min="0" />
                </el-form-item>
                <el-form-item label="状态">
                    <el-radio-group v-model="dataForm.status">
                        <el-radio :value="1">启用</el-radio>
                        <el-radio :value="0">停用</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="dataForm.remark" type="textarea" :rows="2" />
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="dataFormVisible = false">取消</el-button>
                    <el-button type="primary" :loading="saving" @click="saveData">
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
    createDictData,
    createDictType,
    deleteDictData,
    deleteDictType,
    listDictData,
    listDictTypes,
    updateDictData,
    updateDictType,
} from "@/api/system/dict";
import type { SysDictData, SysDictType } from "@/types/system";

const types = ref<SysDictType[]>([]);
const keyword = ref("");
const loading = ref(false);
const saving = ref(false);

const typeDialogVisible = ref(false);
const editingTypeId = ref("");
const typeFormRef = ref<FormInstance>();
const typeForm = reactive<Partial<SysDictType>>({
    dictType: "",
    dictName: "",
    status: 1,
    remark: "",
});
const typeRules: FormRules = {
    dictType: [{ required: true, message: "请输入字典类型", trigger: "blur" }],
    dictName: [{ required: true, message: "请输入字典名称", trigger: "blur" }],
};

const dataDialogVisible = ref(false);
const dataFormVisible = ref(false);
const currentType = ref<SysDictType | null>(null);
const dictData = ref<SysDictData[]>([]);
const dataLoading = ref(false);
const editingDataId = ref("");
const dataFormRef = ref<FormInstance>();
const dataForm = reactive<Partial<SysDictData>>({
    dictType: "",
    label: "",
    dictValue: "",
    sortNo: 0,
    status: 1,
    remark: "",
});
const dataRules: FormRules = {
    label: [{ required: true, message: "请输入显示文本", trigger: "blur" }],
    dictValue: [{ required: true, message: "请输入字典值", trigger: "blur" }],
};

function formatTime(value?: string) {
    return value ? value.replace("T", " ").slice(0, 19) : "-";
}

async function loadTypes() {
    loading.value = true;
    try {
        types.value = await listDictTypes(keyword.value || undefined);
    } finally {
        loading.value = false;
    }
}

function reset() {
    keyword.value = "";
    loadTypes();
}

function openCreateType() {
    editingTypeId.value = "";
    Object.assign(typeForm, {
        dictType: "",
        dictName: "",
        status: 1,
        remark: "",
    });
    typeDialogVisible.value = true;
    nextTick(() => typeFormRef.value?.clearValidate());
}

function openEditType(row: SysDictType) {
    editingTypeId.value = row.id;
    Object.assign(typeForm, {
        dictType: row.dictType,
        dictName: row.dictName,
        status: row.status,
        remark: row.remark ?? "",
    });
    typeDialogVisible.value = true;
    nextTick(() => typeFormRef.value?.clearValidate());
}

async function saveType() {
    if (!typeFormRef.value) {
        return;
    }
    const valid = await typeFormRef.value.validate().catch(() => false);
    if (!valid) {
        return;
    }
    saving.value = true;
    try {
        if (editingTypeId.value) {
            await updateDictType(editingTypeId.value, { ...typeForm });
        } else {
            await createDictType({ ...typeForm });
        }
        ElMessage.success("保存成功");
        typeDialogVisible.value = false;
        await loadTypes();
    } finally {
        saving.value = false;
    }
}

async function removeType(row: SysDictType) {
    const confirmed = await ElMessageBox.confirm(
        `确认删除字典「${row.dictName}」及其全部数据吗？`,
        "提示",
        { type: "warning" },
    )
        .then(() => true)
        .catch(() => false);
    if (!confirmed) {
        return;
    }
    await deleteDictType(row.id);
    ElMessage.success("删除成功");
    await loadTypes();
}

async function loadData() {
    if (!currentType.value) {
        return;
    }
    dataLoading.value = true;
    try {
        dictData.value = await listDictData(currentType.value.dictType);
    } finally {
        dataLoading.value = false;
    }
}

function openData(row: SysDictType) {
    currentType.value = row;
    dataDialogVisible.value = true;
    loadData();
}

function openCreateData() {
    editingDataId.value = "";
    Object.assign(dataForm, {
        dictType: currentType.value?.dictType ?? "",
        label: "",
        dictValue: "",
        sortNo: 0,
        status: 1,
        remark: "",
    });
    dataFormVisible.value = true;
    nextTick(() => dataFormRef.value?.clearValidate());
}

function openEditData(row: SysDictData) {
    editingDataId.value = row.id;
    Object.assign(dataForm, {
        dictType: row.dictType,
        label: row.label,
        dictValue: row.dictValue,
        sortNo: row.sortNo,
        status: row.status,
        remark: row.remark ?? "",
    });
    dataFormVisible.value = true;
    nextTick(() => dataFormRef.value?.clearValidate());
}

async function saveData() {
    if (!dataFormRef.value) {
        return;
    }
    const valid = await dataFormRef.value.validate().catch(() => false);
    if (!valid) {
        return;
    }
    saving.value = true;
    try {
        if (editingDataId.value) {
            await updateDictData(editingDataId.value, { ...dataForm });
        } else {
            await createDictData({ ...dataForm });
        }
        ElMessage.success("保存成功");
        dataFormVisible.value = false;
        await loadData();
    } finally {
        saving.value = false;
    }
}

async function removeData(row: SysDictData) {
    const confirmed = await ElMessageBox.confirm(
        `确认删除字典数据「${row.label}」吗？`,
        "提示",
        { type: "warning" },
    )
        .then(() => true)
        .catch(() => false);
    if (!confirmed) {
        return;
    }
    await deleteDictData(row.id);
    ElMessage.success("删除成功");
    await loadData();
}

onMounted(loadTypes);
</script>
