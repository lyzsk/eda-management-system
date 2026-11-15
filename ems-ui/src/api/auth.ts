import { request } from "@/utils/http";
import type { LoginPayload, LoginResult, UserInfo } from "@/types/api";

export function login(payload: LoginPayload): Promise<LoginResult> {
    return request<LoginResult>({
        url: "/auth/login",
        method: "post",
        data: payload,
    });
}

export function getMe(): Promise<UserInfo> {
    return request<UserInfo>({
        url: "/auth/me",
        method: "get",
    });
}

export function logout(): Promise<void> {
    return request<void>({
        url: "/auth/logout",
        method: "post",
    });
}
