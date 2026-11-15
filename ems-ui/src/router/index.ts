import {
    createRouter,
    createWebHistory,
    type RouteRecordRaw,
} from "vue-router";
import NProgress from "nprogress";
import "nprogress/nprogress.css";
import { useUserStore } from "@/store/modules/user";

NProgress.configure({ showSpinner: false, trickleSpeed: 120 });

const routes: RouteRecordRaw[] = [
    {
        path: "/login",
        name: "Login",
        component: () => import("@/views/login/index.vue"),
        meta: { title: "login" },
    },
    {
        path: "/",
        name: "Layout",
        component: () => import("@/views/index/index.vue"),
        redirect: "/dashboard",
        children: [
            {
                path: "dashboard",
                name: "Dashboard",
                component: () => import("@/views/dashboard/index.vue"),
                meta: { title: "menu.dashboard" },
            },
            {
                path: "system/user",
                name: "SystemUser",
                component: () => import("@/views/system/user/index.vue"),
                meta: { title: "menu.user" },
            },
            {
                path: "system/role",
                name: "SystemRole",
                component: () => import("@/views/system/role/index.vue"),
                meta: { title: "menu.role" },
            },
            {
                path: "system/menu",
                name: "SystemMenu",
                component: () => import("@/views/system/menu/index.vue"),
                meta: { title: "menu.menu" },
            },
            {
                path: "system/dept",
                name: "SystemDept",
                component: () => import("@/views/system/dept/index.vue"),
                meta: { title: "menu.dept" },
            },
            {
                path: "system/dict",
                name: "SystemDict",
                component: () => import("@/views/system/dict/index.vue"),
                meta: { title: "menu.dict" },
            },
            {
                path: "eda/list",
                name: "EdaList",
                component: () => import("@/views/eda/list/index.vue"),
                meta: { title: "menu.edaList" },
            },
            {
                path: "eda/server",
                name: "EdaServer",
                component: () => import("@/views/eda/server/index.vue"),
                meta: { title: "menu.edaServer" },
            },
            {
                path: "eda/script",
                name: "EdaScript",
                component: () => import("@/views/eda/script/index.vue"),
                meta: { title: "menu.edaScript" },
            },
        ],
    },
    {
        path: "/:pathMatch(.*)*",
        name: "NotFound",
        component: () => import("@/views/exception/404/index.vue"),
        meta: { title: "404" },
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach((to) => {
    NProgress.start();
    const userStore = useUserStore();

    if (to.name !== "Login" && !userStore.isLogin) {
        return { name: "Login", query: { redirect: to.fullPath } };
    }

    if (to.name === "Login" && userStore.isLogin) {
        return { path: "/dashboard" };
    }

    return true;
});

router.afterEach(() => {
    NProgress.done();
});

router.onError(() => {
    NProgress.done();
});

export default router;
