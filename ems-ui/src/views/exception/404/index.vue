<template>
    <div class="not-found">
        <div class="code">404</div>
        <p>页面不存在</p>
        <el-button type="primary" @click="router.replace('/dashboard')">
            返回仪表盘
        </el-button>
    </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";

const router = useRouter();
</script>

<style scoped lang="scss">
.not-found {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 60vh;
    gap: 12px;
    color: var(--text);
}

.code {
    font-size: 72px;
    font-weight: 800;
    color: var(--primary);
}

p {
    margin: 0;
    color: var(--text-muted);
}
</style>
