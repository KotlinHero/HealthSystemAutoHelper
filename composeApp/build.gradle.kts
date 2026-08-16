import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation)

            implementation(libs.filekit.dialogs.compose)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            //仅使用material3
            //排除material
            implementation(compose.desktop.currentOs) {
                exclude(group = "org.jetbrains.compose.material", module = "material-desktop")
            }
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(projects.core)
        }
    }
}


compose.desktop {
    application {
        mainClass = "tech.kotlinhero.autohelper.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = project.properties["app.package.name"].toString()
            packageVersion = project.properties["app.version"].toString()
            includeAllModules = true
            if (project.properties["app.bind"].toString().toBoolean()) {
                appResourcesRootDir.set(project.layout.projectDirectory.dir("bin"))
            }
            windows {
                iconFile.set(project.file("src/jvmMain/composeResources/drawable/app.ico"))
                menuGroup = "AutoHelper"
                shortcut = true
                menu = true
            }
        }
    }
}

interface InjectedExecOps {
    @get:Inject val execOps: ExecOperations
}

afterEvaluate {
    if (project.properties["app.bind"].toString().toBoolean()) {
        listOf("packageMsi", "packageDmg", "packageDeb").forEach { taskName ->
            tasks.matching { it.name == taskName }.configureEach {
                dependsOn(":webdriver:installPlaywrightChromium")
            }
        }
    }

    val outputDir: String = layout.buildDirectory.dir("compose/binaries/main/msi").get()
        .asFile
        .absolutePath
    val injected = project.objects.newInstance<InjectedExecOps>()
    tasks.named("packageMsi") {
        doLast {
            injected.execOps.exec {
                commandLine("cmd", "/c", "start", "explorer", outputDir)
            }
        }
    }
}