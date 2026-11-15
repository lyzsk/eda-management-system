import { request } from "@/utils/http";
import type { SysDept } from "@/types/system";

export function listDepts(keyword?: string): Promise<SysDept[]> {
    return request<SysDept[]>({
        url: "/system/depts",
        method: "get",
        params: { keyword },
    });
}

export function createDept(data: Partial<SysDept>): Promise<string> {
    return request<string>({
        url: "/system/depts",
        method: "post",
        data,
    });
}

export function updateDept(id: string, data: Partial<SysDept>): Promise<void> {
    return request<void>({
        url: `/system/depts/${id}`,
        method: "put",
        data,
    });
}

export function deleteDept(id: string): Promise<void> {
    return request<void>({
        url: `/system/depts/${id}`,
        method: "delete",
    });
}
