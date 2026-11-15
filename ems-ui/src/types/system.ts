export interface SysUser {
    id: string;
    username: string;
    realName: string;
    gender: number;
    email?: string;
    mobile?: string;
    avatar?: string;
    deptId?: string;
    deptName?: string;
    roleCode?: string;
    status: number;
    remark?: string;
    createTime?: string;
    updateTime?: string;
}

export interface SysRole {
    id: string;
    roleCode: string;
    roleName: string;
    sortNo: number;
    status: number;
    remark?: string;
    createTime?: string;
    updateTime?: string;
}

export interface SysMenu {
    id: string;
    parentId: string;
    menuCode: string;
    menuName: string;
    menuType: "DIR" | "MENU" | "BUTTON";
    path?: string;
    component?: string;
    permission?: string;
    icon?: string;
    sortNo: number;
    status: number;
    remark?: string;
    createTime?: string;
    updateTime?: string;
    children?: SysMenu[];
}

export interface SysDept {
    id: string;
    parentId: string;
    deptCode: string;
    deptName: string;
    sortNo: number;
    status: number;
    remark?: string;
    createTime?: string;
    updateTime?: string;
    children?: SysDept[];
}

export interface SysDictType {
    id: string;
    dictType: string;
    dictName: string;
    status: number;
    remark?: string;
    createTime?: string;
    updateTime?: string;
}

export interface SysDictData {
    id: string;
    dictType: string;
    label: string;
    dictValue: string;
    sortNo: number;
    status: number;
    remark?: string;
    createTime?: string;
    updateTime?: string;
}

export interface EdaTool {
    id: string;
    toolName: string;
    toolType?: string;
    host?: string;
    osType?: string;
    toolVersion?: string;
    licenseStatus?: string;
    status: number;
    lastHeartbeat?: string;
    serverId?: string;
    remark?: string;
    createTime?: string;
    updateTime?: string;
}

export interface EdaServer {
    id: string;
    serverCode: string;
    serverName?: string;
    host: string;
    sshPort: number;
    osType?: string;
    username?: string;
    connectTimeoutSeconds?: number;
    status: number;
    remark?: string;
    createTime?: string;
    updateTime?: string;
}

export interface EdaServerForm {
    serverCode?: string;
    serverName?: string;
    host?: string;
    sshPort?: number;
    osType?: string;
    username?: string;
    connectTimeoutSeconds?: number;
    status?: number;
    remark?: string;
}

export interface EdaScript {
    id: string;
    toolId: string;
    action: string;
    scriptName: string;
    execMode?: string;
    command: string;
    workdir?: string;
    timeoutSeconds?: number;
    enabled: number;
    sortNo?: number;
    remark?: string;
    createTime?: string;
    updateTime?: string;
}

export interface EdaScriptRun {
    id: string;
    runId: string;
    scriptId: string;
    toolId: string;
    serverId: string;
    action: string;
    operatorName?: string;
    status: string;
    exitCode?: number;
    output?: string;
    errorMsg?: string;
    sshHost?: string;
    startedAt?: string;
    finishedAt?: string;
    createTime?: string;
    updateTime?: string;
}
