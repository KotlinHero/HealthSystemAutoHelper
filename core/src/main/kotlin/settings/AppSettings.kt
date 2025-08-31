package tech.kotlinhero.autohelper.core.settings

import java.util.prefs.Preferences

interface AppSettings {
    var chromeBinaryPath: String
    var chromeDriverPath: String
}

object AppSettingsPreferences : AppSettings {
    private val preferences = Preferences.userNodeForPackage(AppSettings::class.java)

    override var chromeBinaryPath: String
        get() = preferences.get("chrome_binary", "")
        set(value) = preferences.put("chrome_binary", value)

    override var chromeDriverPath: String
        get() = preferences.get("chrome_driver", "")
        set(value) = preferences.put("chrome_driver", value)
}