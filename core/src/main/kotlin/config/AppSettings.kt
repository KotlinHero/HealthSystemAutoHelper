package tech.kotlinhero.autohelper.core.config

interface AppSettings : HealthSystemWebsiteConfig {
    var chromeBinaryPath: String
    var chromeDriverPath: String
}