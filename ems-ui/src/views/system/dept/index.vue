<template>
    <div class="page-card">
        <div class="page-header">
            <div class="page-header-text">
                <h1>部门管理</h1>
                <p>维护组织架构与部门编码</p>
            </div>
            <div class="page-header-actions">
                <el-button type="primary" :icon="Plus" @click="openCreate">
                    新增部门
                </el-button>
            </div>
        </div>

        <div class="toolbar">
            <el-input
                v-model="keyword"
                placeholder="部门名称 / 编码"
                clearable
                style="width: 240px"
                @keyup.enter="load"
            />
            <el-button type="primary" :icon="Search" @click="load">
                搜索
            </el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
        </div>

        <el-table
            v-loading="loading"
            :data="depts"
            row-key="id"
            default-expand-all
            :tree-props="{ children: 'children' }"
        >
            <el-table-column prop="deptName" label="部门名称" min-width="220" />
            <el-table-column prop="deptCode" label="部门编码" min-width="160" />
            <el-table-column prop="sortNo" label="排序" width="90" />
            <el-table-column label="状态" width="100">
                <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'info'">
                        {{ row.status === 1 ? "启用" : "停用" }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="创建时间" width="180">
                <template #default="{ row }">
                    {{ formatTime(row.createTime) }}
                </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
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
            :title="editingId ? '编辑部门' : '新增部门'"
            width="560px"
        >
            <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
                <el-form-item label="上级部门">
                    <el-tree-select
                        v-model="form.parentId"
                        :data="depts"
                        :props="{
                            value: 'id',
                            label: 'deptName',
                            children: 'children',
                        }"
                        :disabled="editingId === '0'"
                        check-strictly
                        style="width: 100%"
                    />
                </el-form-item>
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="部门编码" prop="deptCode">
                            <el-input v-model="form.deptCode" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="部门名称" prop="deptName">
                            <el-input v-model="form.deptName" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="排序">
                            <el-input-number v-model="form.sortNo" :min="0" />
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
    createDept,
    deleteDept,
    listDepts,
    updateDept,
} from "@/api/system/dept";
import type { SysDept } from "@/types/system";

const depts = ref<SysDept[]>([]);
const keyword = ref("");
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const editingId = ref("");
const formRef = ref<FormInstance>();

const form = reactive<Partial<SysDept>>({
    parentId: "0",
    deptCode: "",
    deptName: "",
    sortNo: 0,
    status: 1,
    remark: "",
});

const rules: FormRules = {
    deptCode: [{ required: true, message: "请输入部门编码", trigger: "blur" }],
    deptName: [{ required: true, message: "请输入部门名称", trigger: "blur" }],
};

function formatTime(value?: string) {
    return value ? value.replace("T", " ").slice(0, 19) : "-";
}

async function load() {
    loading.value = true;
    try {
        depts.value = await listDepts(keyword.value || undefined);
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
        parentId: "0",
        deptCode: "",
        deptName: "",
        sortNo: 0,
        status: 1,
        remark: "",
    });
    dialogVisible.value = true;
    nextTick(() => formRef.value?.clearValidate());
}

function openEdit(row: SysDept) {
    editingId.value = row.id;
    Object.assign(form, {
        parentId: row.parentId ?? "0",
        deptCode: row.deptCode,
        deptName: row.deptName,
        sortNo: row.sortNo,
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
        if (editingId.value) {
            await updateDept(editingId.value, { ...form });
        } else {
            await createDept({ ...form });
        }
        ElMessage.success("保存成功");
        dialogVisible.value = false;
        await load();
    } finally {
        saving.value = false;
    }
}

async function remove(row: SysDept) {
    const confirmed = await ElMessageBox.confirm(
        `确认删除部门「${row.deptName}」吗？`,
        "提示",
        { type: "warning" },
    )
        .then(() => true)
        .catch(() => false);
    if (!confirmed) {
        return;
    }
    await deleteDept(row.id);
    ElMessage.success("删除成功");
    await load();
}

onMounted(load);
</script>
