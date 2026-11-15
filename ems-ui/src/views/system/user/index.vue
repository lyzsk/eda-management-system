<template>
    <div class="page-card">
        <div class="page-header">
            <div class="page-header-text">
                <h1>用户管理</h1>
                <p>管理系统用户、所属部门与角色</p>
            </div>
            <div class="page-header-actions">
                <el-button type="primary" :icon="Plus" @click="openCreate">
                    新增用户
                </el-button>
            </div>
        </div>

        <div class="toolbar">
            <el-input
                v-model="query.keyword"
                placeholder="用户名 / 姓名"
                clearable
                style="width: 220px"
                @keyup.enter="load"
            />
            <el-select
                v-model="query.status"
                placeholder="状态"
                clearable
                style="width: 140px"
            >
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="load">
                搜索
            </el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
        </div>

        <el-table v-loading="loading" :data="users">
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column prop="realName" label="姓名" min-width="100" />
            <el-table-column prop="deptName" label="部门" min-width="120" />
            <el-table-column prop="email" label="邮箱" min-width="180" />
            <el-table-column prop="mobile" label="手机号" min-width="130" />
            <el-table-column prop="roleCode" label="角色" width="100" />
            <el-table-column label="状态" width="90">
                <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'info'">
                        {{ row.status === 1 ? "启用" : "停用" }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="创建时间" width="170">
                <template #default="{ row }">
                    {{ formatTime(row.createTime) }}
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
            :title="editingId ? '编辑用户' : '新增用户'"
            width="600px"
        >
            <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="用户名" prop="username">
                            <el-input
                                v-model="form.username"
                                :disabled="!!editingId"
                                placeholder="登录账号"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="姓名" prop="realName">
                            <el-input v-model="form.realName" placeholder="真实姓名" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="密码">
                            <el-input
                                v-model="form.password"
                                type="password"
                                show-password
                                :placeholder="
                                    editingId ? '不填则不修改' : '默认 admin123'
                                "
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="部门">
                            <el-select
                                v-model="form.deptId"
                                clearable
                                placeholder="选择部门"
                                style="width: 100%"
                            >
                                <el-option
                                    v-for="dept in deptOptions"
                                    :key="dept.id"
                                    :label="dept.deptName"
                                    :value="dept.id"
                                />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="邮箱">
                            <el-input v-model="form.email" placeholder="邮箱" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="手机号">
                            <el-input v-model="form.mobile" placeholder="手机号" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="性别">
                            <el-select v-model="form.gender" style="width: 100%">
                                <el-option label="未知" :value="0" />
                                <el-option label="男" :value="1" />
                                <el-option label="女" :value="2" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="角色">
                            <el-select v-model="form.roleCode" style="width: 100%">
                                <el-option label="超级管理员" value="ADMIN" />
                                <el-option label="普通用户" value="USER" />
                            </el-select>
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
                            <el-input
                                v-model="form.remark"
                                type="textarea"
                                :rows="2"
                                placeholder="备注"
                            />
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
    createUser,
    deleteUser,
    listUsers,
    updateUser,
} from "@/api/system/user";
import { listDepts } from "@/api/system/dept";
import type { SysDept, SysUser } from "@/types/system";

interface UserForm extends Partial<SysUser> {
    password?: string;
}

const users = ref<SysUser[]>([]);
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const editingId = ref("");
const formRef = ref<FormInstance>();

const query = reactive<{ keyword: string; status?: number }>({
    keyword: "",
    status: undefined,
});

const form = reactive<UserForm>({
    username: "",
    realName: "",
    password: "",
    gender: 1,
    deptId: undefined,
    email: "",
    mobile: "",
    roleCode: "USER",
    status: 1,
    remark: "",
});

const rules: FormRules = {
    username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
    realName: [{ required: true, message: "请输入姓名", trigger: "blur" }],
    email: [
        {
            type: "email",
            message: "邮箱格式不正确",
            trigger: ["blur", "change"],
        },
    ],
};

const deptOptions = ref<SysDept[]>([]);

function flattenDepts(nodes: SysDept[], result: SysDept[] = []): SysDept[] {
    for (const node of nodes) {
        result.push(node);
        if (node.children?.length) {
            flattenDepts(node.children, result);
        }
    }
    return result;
}

function formatTime(value?: string) {
    return value ? value.replace("T", " ").slice(0, 19) : "-";
}

async function load() {
    loading.value = true;
    try {
        users.value = await listUsers({
            keyword: query.keyword || undefined,
            status: query.status,
        });
    } finally {
        loading.value = false;
    }
}

async function loadDepts() {
    const tree = await listDepts();
    deptOptions.value = flattenDepts(tree);
}

function reset() {
    query.keyword = "";
    query.status = undefined;
    load();
}

function resetForm() {
    Object.assign(form, {
        username: "",
        realName: "",
        password: "",
        gender: 1,
        deptId: undefined,
        email: "",
        mobile: "",
        roleCode: "USER",
        status: 1,
        remark: "",
    });
}

function openCreate() {
    editingId.value = "";
    resetForm();
    dialogVisible.value = true;
    nextTick(() => formRef.value?.clearValidate());
}

function openEdit(row: SysUser) {
    editingId.value = row.id;
    Object.assign(form, {
        username: row.username,
        realName: row.realName,
        password: "",
        gender: row.gender ?? 0,
        deptId: row.deptId ?? undefined,
        email: row.email ?? "",
        mobile: row.mobile ?? "",
        roleCode: row.roleCode ?? "USER",
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
            await updateUser(editingId.value, { ...form });
        } else {
            await createUser({ ...form });
        }
        ElMessage.success("保存成功");
        dialogVisible.value = false;
        await load();
    } finally {
        saving.value = false;
    }
}

async function remove(row: SysUser) {
    const confirmed = await ElMessageBox.confirm(
        `确认删除用户「${row.username}」吗？`,
        "提示",
        { type: "warning" },
    )
        .then(() => true)
        .catch(() => false);
    if (!confirmed) {
        return;
    }
    await deleteUser(row.id);
    ElMessage.success("删除成功");
    await load();
}

onMounted(async () => {
    await Promise.all([load(), loadDepts()]);
});
</script>
