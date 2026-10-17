# 卫生信息系统自动助手（AutoHelper）

Windows 桌面端 Kotlin Multiplatform（Compose Multiplatform，仅 JVM/Desktop）应用。通过 Selenium 驱动 Chrome 自动登录基层卫生信息系统，把 Excel 中的数据批量录入系统。

> 项目由同名 WPF 版本迁移而来。开发约定、模块边界与提交规范见 [AGENTS.md](AGENTS.md)，版本变更见 [CHANGELOG.md](CHANGELOG.md)。

## 功能

- 高血压 / 糖尿病随访导入
- 高血压 / 糖尿病风险分层（分组评估）导入
- 签约服务导入与档案保存
- 健康体检表新建 / 完善

交互流程：选择任务 → 选择 Excel、输入账号密码 → 执行长任务，界面实时展示总条数、完成数、日志，并支持取消。

## 环境要求

- Windows 10/11
- JDK 17+（构建使用 Gradle Toolchain，缺失时由 foojay resolver 自动下载）
- Chrome / Chrome Headless Shell 与对应版本 chromedriver，在应用「设置」页配置路径

## 构建与运行

在仓库根目录执行（Windows PowerShell）：

```powershell
.\gradlew.bat :composeApp:run                 # 运行应用
.\gradlew.bat :composeApp:hotRunJvm           # 热重载运行（开发用）
.\gradlew.bat build                           # 全量构建
.\gradlew.bat :composeApp:packageMsi          # 打 MSI 安装包（需带 jmods 的完整 JDK）
.\gradlew.bat :composeApp:createDistributable # 生成 app image
```

提交前自检（要求 0 条警告、0 条废弃告警）：

```powershell
.\gradlew.bat :core:compileKotlin :webdriver:compileKotlin :excel:compileKotlin :composeApp:compileKotlinJvm --warning-mode all
.\gradlew.bat :composeApp:jvmTest
```

## 配置

账号密码、Chrome 路径、系统地址等通过应用内「设置」页配置，持久化在 `java.util.prefs`（`core/config/AppPreferences.kt`）。默认系统地址见 `core/config/HealthSystemWebsiteConfig.kt` 中的 `HEALTH_SYSTEM_WEBSITE_URL`。

## 模块结构

依赖方向：`composeApp → core → webdriver / excel`。

- `composeApp/`：Compose 桌面 UI（代码只放在 `jvmMain`）
- `core/`：任务框架、业务导入逻辑、Excel 行模型
- `excel/`：Apache POI 封装
- `webdriver/`：自研 Selenium DSL

## 常见问题

- **Gradle 下载超时**：`gradle/wrapper/gradle-wrapper.properties` 已指向国内镜像，直连 `services.gradle.org` 可能超时。
- **打包报「没有 JDK 的 jmods 模块」**：`jlink` 需要带 `jmods` 目录的完整 JDK，换 JDK 即可，与项目代码无关。
- **测试需要本机 Excel/chromedriver**：历史遗留测试依赖个人文件，当前基线只保留不依赖外部资源的冒烟测试。
