package tech.kotlinhero.autohelper.core.config

import java.util.prefs.Preferences

object AppPreferences : AppSettings, HyperVisitParamsCache, DiabetesParamsCache, ContractParamsCache {

    private val preferences = Preferences.userNodeForPackage(AppSettings::class.java)

    override var diabetesUsername: String
        get() = preferences.get("diabetes_visit_username", "")
        set(value) = preferences.put("diabetes_visit_username", value)

    override var diabetesPassword: String
        get() = preferences.get("diabetes_visit_password", "")
        set(value) = preferences.put("diabetes_visit_password", value)

    override var healthSystemWebsiteUrl: String
        get() = preferences.get("health_system_website_url", HEALTH_SYSTEM_WEBSITE_URL)
        set(value) = preferences.put("health_system_website_url", value)

    override var hyperVisitUsername: String
        get() = preferences.get("hyper_visit_username", "")
        set(value) = preferences.put("hyper_visit_username", value)

    override var hyperVisitPassword: String
        get() = preferences.get("hyper_visit_password", "")
        set(value) = preferences.put("hyper_visit_password", value)

    override var contractUsername: String
        get() = preferences.get("contract_username", "")
        set(value) = preferences.put("contract_username", value)

    override var contractPassword: String
        get() = preferences.get("contract_password", "")
        set(value) = preferences.put("contract_password", value)

    override var browserExecutablePath: String
        get() {
            val newValue = preferences.get("browser_executable_path", "")
            if (newValue.isNotEmpty()) return newValue
            // 兼容旧版 chrome_binary 配置
            return preferences.get("chrome_binary", "")
        }
        set(value) {
            preferences.put("browser_executable_path", value)
        }
}
