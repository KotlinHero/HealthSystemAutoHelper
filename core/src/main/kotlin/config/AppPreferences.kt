package tech.kotlinhero.autohelper.core.config

import java.util.prefs.Preferences

object AppPreferences : AppSettings, HyperVisitParamsCache {

    private val preferences = Preferences.userNodeForPackage(AppSettings::class.java)

    override var healthSystemWebsiteUrl: String
        get() = preferences.get("health_system_website_url", HEALTH_SYSTEM_WEBSITE_URL)
        set(value) = preferences.put("health_system_website_url", value)

    override var hyperVisitUsername: String
        get() = preferences.get("hyper_visit_username", "")
        set(value) = preferences.put("hyper_visit_username", value)

    override var hyperVisitPassword: String
        get() = preferences.get("hyper_visit_password", "")
        set(value) = preferences.put("hyper_visit_password", value)

    override var chromeBinaryPath: String
        get() = preferences.get("chrome_binary", "")
        set(value) = preferences.put("chrome_binary", value)

    override var chromeDriverPath: String
        get() = preferences.get("chrome_driver", "")
        set(value) = preferences.put("chrome_driver", value)
}