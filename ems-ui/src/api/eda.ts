import { request } from "@/utils/http";
import type { EdaTool } from "@/types/system";

export function listEdaTools(keyword?: string): Promise<EdaTool[]> {
    return request<EdaTool[]>({
        url: "/eda/tools",
        method: "get",
        params: { keyword },
    });
}

export function createEdaTool(data: Partial<EdaTool>): Promise<string> {
    return request<string>({
        url: "/eda/tools",
        method: "post",
        data,
    });
}

export function updateEdaTool(
    id: string,
    data: Partial<EdaTool>,
): Promise<void> {
    return request<void>({
        url: `/eda/tools/${id}`,
        method: "put",
        data,
    });
}

export function deleteEdaTool(id: string): Promise<void> {
    return request<void>({
        url: `/eda/tools/${id}`,
        method: "delete",
    });
}
