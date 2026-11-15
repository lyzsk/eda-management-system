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
    remark?: string;
    createTime?: string;
    updateTime?: string;
}
