plugins {
    kotlin("jvm")
}

group = "tech.kotlinhero.autohelper.webdriver"
version = "1.0.0"

dependencies {
    api(libs.selenium.api)
    implementation(libs.selenium.java)
    implementation(libs.selenium.chromedriver)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}