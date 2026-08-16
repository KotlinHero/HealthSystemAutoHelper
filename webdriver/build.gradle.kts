plugins {
    kotlin("jvm")
}

group = "tech.kotlinhero.autohelper.webdriver"
version = "1.0.0"

dependencies {
    api(libs.playwright)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("installPlaywrightChromium") {
    group = "playwright"
    description = "Installs the Playwright Chromium browser into composeApp/bin/common/ms-playwright"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.microsoft.playwright.CLI")
    args("install", "chromium")
    environment(
        "PLAYWRIGHT_BROWSERS_PATH",
        rootProject.layout.projectDirectory.dir("composeApp/bin/common/ms-playwright").asFile.absolutePath
    )
}

kotlin {
    jvmToolchain(17)
}