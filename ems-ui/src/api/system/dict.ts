import { request } from "@/utils/http";
import type { SysDictData, SysDictType } from "@/types/system";

export function listDictTypes(keyword?: string): Promise<SysDictType[]> {
    return request<SysDictType[]>({
        url: "/system/dict/types",
        method: "get",
        params: { keyword },
    });
}

export function createDictType(data: Partial<SysDictType>): Promise<string> {
    return request<string>({
        url: "/system/dict/types",
        method: "post",
        data,
    });
}

export function updateDictType(
    id: string,
    data: Partial<SysDictType>,
): Promise<void> {
    return request<void>({
        url: `/system/dict/types/${id}`,
        method: "put",
        data,
    });
}

export function deleteDictType(id: string): Promise<void> {
    return request<void>({
        url: `/system/dict/types/${id}`,
        method: "delete",
    });
}

export function listDictData(dictType?: string): Promise<SysDictData[]> {
    return request<SysDictData[]>({
        url: "/system/dict/data",
        method: "get",
        params: { dictType },
    });
}

export function createDictData(data: Partial<SysDictData>): Promise<string> {
    return request<string>({
        url: "/system/dict/data",
        method: "post",
        data,
    });
}

export function updateDictData(
    id: string,
    data: Partial<SysDictData>,
): Promise<void> {
    return request<void>({
        url: `/system/dict/data/${id}`,
        method: "put",
        data,
    });
}

export function deleteDictData(id: string): Promise<void> {
    return request<void>({
        url: `/system/dict/data/${id}`,
        method: "delete",
    });
}
