import { computed, ref } from "vue";
import { defineStore } from "pinia";
import type { LoginResult, UserInfo } from "@/types/api";

export const useUserStore = defineStore(
    "user",
    () => {
        const token = ref("");
        const user = ref<UserInfo | null>(null);
        const isLogin = computed(() => token.value.length > 0);
        const roles = computed(() => user.value?.roles ?? []);
        const permissions = computed(() => user.value?.permissions ?? []);

        function setLogin(result: LoginResult) {
            token.value = result.token;
            user.value = result.user;
        }

        function setUser(data: UserInfo) {
            user.value = data;
        }

        function logout() {
            token.value = "";
            user.value = null;
        }

        return {
            token,
            user,
            isLogin,
            roles,
            permissions,
            setLogin,
            setUser,
            logout,
        };
    },
    {
        persist: {
            key: "ems-user",
        },
    },
);
