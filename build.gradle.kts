import org.jetbrains.changelog.ChangelogSectionUrlBuilder
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

val projectVersion = project.property("projectVersion").toString()

changelog {
    version = projectVersion
    path = file("CHANGELOG.md").canonicalPath
    header = provider { "[${version.get()}] - ${date()}" }
    introduction =
        """
        My awesome project that provides a lot of useful features, like:
        
        - Feature 1
        - Feature 2
        - and Feature 3
        """.trimIndent()
    itemPrefix = "-"
    keepUnreleasedSection = true
    unreleasedTerm = "[Unreleased]"
    groups = listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security")
    lineSeparator = "\n"
    combinePreReleases = true
    sectionUrlBuilder = ChangelogSectionUrlBuilder { repositoryUrl, currentVersion, previousVersion, isUnreleased ->
        "foo"
    }
    outputFile = file("release-note.txt")
}
