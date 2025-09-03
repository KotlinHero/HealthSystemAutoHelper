package tech.kotlinhero.autohelper.core.config

import tech.kotlinhero.autohelper.core.settings.AppSettings
import java.util.prefs.Preferences

interface HyperVisitParamsPreferences {
    var hyperVisitUsername: String
    var hyperVisitPassword: String
}

object AppPreferences : AppSettings, HyperVisitParamsPreferences {
    private val preferences = Preferences.userNodeForPackage(AppSettings::class.java)

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