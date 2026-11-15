import { request } from "@/utils/http";
import type { SysRole } from "@/types/system";

export function listRoles(keyword?: string): Promise<SysRole[]> {
    return request<SysRole[]>({
        url: "/system/roles",
        method: "get",
        params: { keyword },
    });
}

export function createRole(data: Partial<SysRole>): Promise<string> {
    return request<string>({
        url: "/system/roles",
        method: "post",
        data,
    });
}

export function updateRole(id: string, data: Partial<SysRole>): Promise<void> {
    return request<void>({
        url: `/system/roles/${id}`,
        method: "put",
        data,
    });
}

export function deleteRole(id: string): Promise<void> {
    return request<void>({
        url: `/system/roles/${id}`,
        method: "delete",
    });
}
