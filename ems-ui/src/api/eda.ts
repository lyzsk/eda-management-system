import { request } from "@/utils/http";
import type {
    EdaScript,
    EdaScriptRun,
    EdaServer,
    EdaServerForm,
    EdaTool,
} from "@/types/system";

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

// ---------- 服务器 ----------

export function listServers(keyword?: string): Promise<EdaServer[]> {
    return request<EdaServer[]>({
        url: "/eda/servers",
        method: "get",
        params: { keyword },
    });
}

export function createServer(data: EdaServerForm): Promise<string> {
    return request<string>({
        url: "/eda/servers",
        method: "post",
        data,
    });
}

export function updateServer(
    id: string,
    data: EdaServerForm,
): Promise<void> {
    return request<void>({
        url: `/eda/servers/${id}`,
        method: "put",
        data,
    });
}

export function deleteServer(id: string): Promise<void> {
    return request<void>({
        url: `/eda/servers/${id}`,
        method: "delete",
    });
}

export interface ExecResult {
    exitCode: number;
    output: string;
    errorMsg: string;
    timedOut: boolean;
}

export function testServer(id: string): Promise<ExecResult> {
    return request<ExecResult>({
        url: `/eda/servers/${id}/test`,
        method: "post",
    });
}

// ---------- 脚本 ----------

export function listScripts(toolId?: string): Promise<EdaScript[]> {
    return request<EdaScript[]>({
        url: "/eda/scripts",
        method: "get",
        params: { toolId },
    });
}

export function createScript(data: Partial<EdaScript>): Promise<string> {
    return request<string>({
        url: "/eda/scripts",
        method: "post",
        data,
    });
}

export function updateScript(
    id: string,
    data: Partial<EdaScript>,
): Promise<void> {
    return request<void>({
        url: `/eda/scripts/${id}`,
        method: "put",
        data,
    });
}

export function deleteScript(id: string): Promise<void> {
    return request<void>({
        url: `/eda/scripts/${id}`,
        method: "delete",
    });
}

export function runScript(id: string): Promise<string> {
    return request<string>({
        url: `/eda/scripts/${id}/run`,
        method: "post",
    });
}

export function getScriptRun(runId: string): Promise<EdaScriptRun> {
    return request<EdaScriptRun>({
        url: `/eda/scripts/runs/${runId}`,
        method: "get",
    });
}

export function listScriptRuns(
    toolId?: string,
    limit = 50,
): Promise<EdaScriptRun[]> {
    return request<EdaScriptRun[]>({
        url: "/eda/scripts/runs",
        method: "get",
        params: { toolId, limit },
    });
}
