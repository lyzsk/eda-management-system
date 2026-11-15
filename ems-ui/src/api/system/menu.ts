import { request } from "@/utils/http";
import type { SysMenu } from "@/types/system";

export function listMenus(keyword?: string): Promise<SysMenu[]> {
    return request<SysMenu[]>({
        url: "/system/menus",
        method: "get",
        params: { keyword },
    });
}

export function createMenu(data: Partial<SysMenu>): Promise<string> {
    return request<string>({
        url: "/system/menus",
        method: "post",
        data,
    });
}

export function updateMenu(id: string, data: Partial<SysMenu>): Promise<void> {
    return request<void>({
        url: `/system/menus/${id}`,
        method: "put",
        data,
    });
}

export function deleteMenu(id: string): Promise<void> {
    return request<void>({
        url: `/system/menus/${id}`,
        method: "delete",
    });
}
