import axios, { type AxiosRequestConfig } from "axios";
import { ElMessage } from "element-plus";
import { AppConfig } from "@/config";
import { useUserStore } from "@/store/modules/user";
import type { ApiResult } from "@/types/api";

const service = axios.create({
    baseURL: AppConfig.api.baseURL,
    timeout: AppConfig.api.timeout,
});

service.interceptors.request.use((config) => {
    const token = useUserStore().token;
    if (token) {
        config.headers.set("Authorization", `Bearer ${token}`);
    }
    return config;
});

service.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            useUserStore().logout();
            if (window.location.pathname !== "/login") {
                window.location.href = "/login";
            }
        }
        return Promise.reject(error);
    },
);

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
    const response = await service.request<ApiResult<T>>(config);
    const body = response.data;

    if (body.code !== "A0001") {
        ElMessage.error(body.msg || "请求失败");
        throw new Error(body.msg || "REQUEST_FAILED");
    }

    return body.data;
}
