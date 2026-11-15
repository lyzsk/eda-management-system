export const AppConfig = {
    systemInfo: {
        name: "EDA 管理平台",
        version: "1.0.0",
    },
    api: {
        baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
        timeout: 15000,
    },
} as const;
