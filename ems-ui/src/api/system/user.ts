import { request } from "@/utils/http";
import type { SysUser } from "@/types/system";

export interface UserQuery {
    keyword?: string;
    status?: number;
}

export function listUsers(params?: UserQuery): Promise<SysUser[]> {
    return request<SysUser[]>({
        url: "/system/users",
        method: "get",
        params,
    });
}

export function createUser(data: Partial<SysUser>): Promise<string> {
    return request<string>({
        url: "/system/users",
        method: "post",
        data,
    });
}

export function updateUser(id: string, data: Partial<SysUser>): Promise<void> {
    return request<void>({
        url: `/system/users/${id}`,
        method: "put",
        data,
    });
}

export function deleteUser(id: string): Promise<void> {
    return request<void>({
        url: `/system/users/${id}`,
        method: "delete",
    });
}
