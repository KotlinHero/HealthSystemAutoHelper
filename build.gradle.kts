import org.jetbrains.changelog.date

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

    alias(libs.plugins.changelog)
}

val projectVersion = providers.gradleProperty("app.version").get()

changelog {
    version = projectVersion
    path = file("CHANGELOG.md").canonicalPath
    header = provider { "[${version.get()}] - ${date()}" }
    introduction = provider { "卫生信息系统自动助手（AutoHelper）的版本变更记录，格式基于 Keep a Changelog。" }
    itemPrefix = "-"
    keepUnreleasedSection = true
    unreleasedTerm = "[Unreleased]"
    groups = listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security")
    lineSeparator = "\n"
    combinePreReleases = true
    repositoryUrl = "https://github.com/KotlinHero/HealthSystemAutoHelper"
    outputFile = file("release-note.txt")
}
