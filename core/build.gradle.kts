plugins {
    kotlin("jvm")
}

group = "tech.kotlinhero.autohelper.core"
version = "1.0.0"

dependencies {
    implementation(projects.webdriver)
    implementation(projects.excel)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
