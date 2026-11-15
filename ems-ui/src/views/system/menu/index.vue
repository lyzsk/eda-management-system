<template>
    <div class="page-card">
        <div class="page-header">
            <div class="page-header-text">
                <h1>菜单管理</h1>
                <p>维护目录、页面与按钮权限</p>
            </div>
            <div class="page-header-actions">
                <el-button type="primary" :icon="Plus" @click="openCreate">
                    新增菜单
                </el-button>
            </div>
        </div>

        <div class="toolbar">
            <el-input
                v-model="keyword"
                placeholder="菜单名称 / 编码"
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
            :data="menus"
            row-key="id"
            default-expand-all
            :tree-props="{ children: 'children' }"
        >
            <el-table-column prop="menuName" label="菜单名称" min-width="180" />
            <el-table-column prop="menuCode" label="菜单编码" min-width="150" />
            <el-table-column label="类型" width="100">
                <template #default="{ row }">
                    <el-tag :type="typeTag(row.menuType)">
                        {{ typeLabel(row.menuType) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="path" label="路由" min-width="150" />
            <el-table-column prop="component" label="组件" min-width="170" />
            <el-table-column prop="permission" label="权限码" min-width="150" />
            <el-table-column prop="sortNo" label="排序" width="80" />
            <el-table-column label="状态" width="90">
                <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'info'">
                        {{ row.status === 1 ? "启用" : "停用" }}
                    </el-tag>
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
            :title="editingId ? '编辑菜单' : '新增菜单'"
            width="640px"
        >
            <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="上级菜单">
                            <el-tree-select
                                v-model="form.parentId"
                                :data="parentOptions"
                                :props="{
                                    value: 'id',
                                    label: 'menuName',
                                    children: 'children',
                                }"
                                check-strictly
                                style="width: 100%"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="菜单类型">
                            <el-radio-group v-model="form.menuType">
                                <el-radio value="DIR">目录</el-radio>
                                <el-radio value="MENU">页面</el-radio>
                                <el-radio value="BUTTON">按钮</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="菜单名称" prop="menuName">
                            <el-input v-model="form.menuName" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="菜单编码" prop="menuCode">
                            <el-input v-model="form.menuCode" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="路由">
                            <el-input v-model="form.path" placeholder="/system/user" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="组件">
                            <el-input
                                v-model="form.component"
                                placeholder="system/user/index"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="权限码">
                            <el-input
                                v-model="form.permission"
                                placeholder="system:user:list"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="图标">
                            <el-input v-model="form.icon" placeholder="ri:user-3-line" />
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
import { computed, nextTick, onMounted, reactive, ref } from "vue";
import {
    ElMessage,
    ElMessageBox,
    type FormInstance,
    type FormRules,
} from "element-plus";
import { Plus, Refresh, Search } from "@element-plus/icons-vue";
import {
    createMenu,
    deleteMenu,
    listMenus,
    updateMenu,
} from "@/api/system/menu";
import type { SysMenu } from "@/types/system";

const menus = ref<SysMenu[]>([]);
const keyword = ref("");
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const editingId = ref("");
const formRef = ref<FormInstance>();

const form = reactive<Partial<SysMenu>>({
    parentId: "0",
    menuName: "",
    menuCode: "",
    menuType: "MENU",
    path: "",
    component: "",
    permission: "",
    icon: "",
    sortNo: 0,
    status: 1,
    remark: "",
});

const rules: FormRules = {
    menuName: [{ required: true, message: "请输入菜单名称", trigger: "blur" }],
    menuCode: [{ required: true, message: "请输入菜单编码", trigger: "blur" }],
};

const parentOptions = computed(() => [
    { id: "0", menuName: "根目录", children: menus.value },
]);

function typeLabel(type?: string) {
    return type === "DIR" ? "目录" : type === "BUTTON" ? "按钮" : "页面";
}

function typeTag(type?: string) {
    return type === "DIR" ? "warning" : type === "BUTTON" ? "info" : "success";
}

async function load() {
    loading.value = true;
    try {
        menus.value = await listMenus(keyword.value || undefined);
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
        menuName: "",
        menuCode: "",
        menuType: "MENU",
        path: "",
        component: "",
        permission: "",
        icon: "",
        sortNo: 0,
        status: 1,
        remark: "",
    });
    dialogVisible.value = true;
    nextTick(() => formRef.value?.clearValidate());
}

function openEdit(row: SysMenu) {
    editingId.value = row.id;
    Object.assign(form, {
        parentId: row.parentId ?? "0",
        menuName: row.menuName,
        menuCode: row.menuCode,
        menuType: row.menuType,
        path: row.path ?? "",
        component: row.component ?? "",
        permission: row.permission ?? "",
        icon: row.icon ?? "",
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
            await updateMenu(editingId.value, { ...form });
        } else {
            await createMenu({ ...form });
        }
        ElMessage.success("保存成功");
        dialogVisible.value = false;
        await load();
    } finally {
        saving.value = false;
    }
}

async function remove(row: SysMenu) {
    const confirmed = await ElMessageBox.confirm(
        `确认删除菜单「${row.menuName}」吗？`,
        "提示",
        { type: "warning" },
    )
        .then(() => true)
        .catch(() => false);
    if (!confirmed) {
        return;
    }
    await deleteMenu(row.id);
    ElMessage.success("删除成功");
    await load();
}

onMounted(load);
</script>
