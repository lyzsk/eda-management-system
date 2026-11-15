import { createI18n } from "vue-i18n";
import zhCN from "./zh-CN";
import en from "./en";

function readLocale(): "zh-CN" | "en" {
    try {
        const raw = localStorage.getItem("ems-app");
        if (raw) {
            const saved = JSON.parse(raw) as { locale?: string };
            if (saved.locale === "en") {
                return "en";
            }
        }
    } catch {
        // ignore invalid persisted state
    }
    return "zh-CN";
}

const i18n = createI18n({
    legacy: false,
    locale: readLocale(),
    fallbackLocale: "zh-CN",
    messages: {
        "zh-CN": zhCN,
        en,
    },
});

export default i18n;
