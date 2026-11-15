export interface ApiResult<T = unknown> {
    code: string;
    msg: string;
    data: T;
}

export interface UserInfo {
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
    roles?: string[];
    permissions?: string[];
}

export interface LoginPayload {
    username: string;
    password: string;
}

export interface LoginResult {
    token: string;
    user: UserInfo;
}
