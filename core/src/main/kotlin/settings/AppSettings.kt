package tech.kotlinhero.autohelper.core.settings

import java.util.prefs.Preferences

interface AppSettings {
    var chromeBinaryPath: String
    var chromeDriverPath: String
}

