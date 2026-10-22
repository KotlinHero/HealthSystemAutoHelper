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
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.filekit.dialogs.compose)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            // Compose Desktop：本仓库只发布 Windows x64，OS 专用聚合包内含 skiko native
            implementation(libs.compose.desktop)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(projects.core)
        }
    }
}


val appPackageName: String = providers.gradleProperty("app.package.name").get()
val appVersion: String = providers.gradleProperty("app.version").get()
val appBind: Boolean = providers.gradleProperty("app.bind").get().toBoolean()

// 设置页「关于」显示的版本号：构建时从 gradle.properties 的 app.version 生成，避免两处硬编码版本
abstract class GenerateAppInfo : DefaultTask() {
    @get:Input
    abstract val version: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile.resolve("tech/kotlinhero/autohelper")
        dir.mkdirs()
        dir.resolve("AppInfo.kt").writeText(
            """
            |package tech.kotlinhero.autohelper
            |
            |/** 构建时写入的应用版本，供设置页「关于」显示 */
            |object AppInfo {
            |    const val version: String = "${version.get()}"
            |}
            |""".trimMargin(),
        )
    }
}

val generateAppInfo = tasks.register<GenerateAppInfo>("generateAppInfo") {
    version.set(providers.gradleProperty("app.version"))
    outputDir.set(layout.buildDirectory.dir("generated/appInfo/jvmMain/kotlin"))
}
kotlin.sourceSets.named("jvmMain") { kotlin.srcDir(layout.buildDirectory.dir("generated/appInfo/jvmMain/kotlin")) }
tasks.matching { it.name.startsWith("compile") }.configureEach { dependsOn(generateAppInfo) }

compose.desktop {
    application {
        mainClass = "tech.kotlinhero.autohelper.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = appPackageName
            packageVersion = appVersion
            includeAllModules = true
            if (appBind) {
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