plugins {
    kotlin("jvm")
}

group = "tech.kotlinhero.autohelper.excel"
version = "1.0.0"

dependencies {
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}