plugins {
    kotlin("jvm")
}

group = "tech.kotlinhero.autohelper.excel"
version = "1.0.0"

dependencies {
    api(libs.poi)
    implementation(libs.poi.ooxml)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}