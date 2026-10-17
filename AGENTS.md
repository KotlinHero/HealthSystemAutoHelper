# AGENTS.md

面向 AI 编码 agent 与协作者的仓库工作指南。动代码前请先读完本文件；结构与命令发生变化时，请顺手更新本文件。

> **硬性约束：每完成一项工作（功能、修复、重构、文档、构建配置等）就立即提交，不允许把已完成的改动留在工作区。** 提交规范见第 6 节，提交前检查清单见第 8 节；push 仍需用户明确要求。

## 1. 项目是什么

- **卫生信息系统自动助手（AutoHelper）**：Windows 桌面端 Kotlin Multiplatform（Compose Multiplatform，仅 JVM/Desktop）应用，包名 `tech.kotlinhero.autohelper`。
- 通过 Selenium 驱动 Chrome 自动登录基层卫生信息系统（`http://60.204.158.212:30050/lsehr/index.html`，可配置），把 Excel 中的数据批量录入系统：
  - 高血压 / 糖尿病随访导入
  - 高血压 / 糖尿病风险分层
  - 签约服务导入与保存
  - 健康体检表创建 / 完善
- 交互流程：选择任务 → 选择 Excel、输入账号密码 → 执行长任务，UI 实时展示总条数、完成数、日志与取消。
- 项目由同名 WPF 版本迁移而来，功能项与迁移记录见 [CHANGELOG.md](CHANGELOG.md)。

## 2. 技术栈与版本

版本唯一来源是 [gradle/libs.versions.toml](gradle/libs.versions.toml)，构建脚本一律通过 `libs.xxx` 引用，**不要硬编码版本**。

| 组件 | 版本 |
| --- | --- |
| Kotlin | 2.4.21（context parameters 已稳定，无需 `-Xcontext-parameters`） |
| Compose Multiplatform | 1.12.2（Compose Compiler 随 Kotlin） |
| Compose Material3 | 1.12.0-alpha03（CMP 的 material3 与 CMP 版本解耦，独立发版） |
| compose material-icons-extended | 1.7.3（官方已冻结，不随 CMP 升级） |
| compose-hot-reload | 1.3.0-beta02（预发布；CMP 1.10+ 会内置 1.2.0，显式声明本版本以覆盖内置版本） |
| Gradle Wrapper | 9.8.1 |
| JDK Toolchain | 17（`jvmToolchain(17)`，foojay resolver 可自动下载） |
| Selenium | 4.51.0 |
| Apache POI | 5.5.1 |
| kotlinx-coroutines | 1.11.0 |
| androidx lifecycle | 2.11.0 |
| androidx navigation-compose | 2.10.0-rc01 |
| filekit（文件选择器） | 0.16.0 |
| kotlinx-changelog | 2.5.0 |
| JUnit | 4.13.2（另有 koin 4.2.2、arrow 2.2.3 仅声明未使用） |

> Compose 依赖统一走版本目录里的显式坐标（`libs.compose.*`），不要再用插件提供的 `compose.runtime` / `compose.desktop.currentOs` 等访问器——CMP 1.10 起它们已废弃。桌面运行时依赖是 `org.jetbrains.compose.desktop:desktop-jvm-windows-x64`（聚合 `desktop` 不含 skiko native，直接引用会在启动时抛 `skiko-windows-x64.dll not found`）。

> `koin-core`、`arrow-core` 只在版本目录中声明，**代码里并未使用**（没有 DI 容器，ViewModel 用 `viewModel { }` 直接创建），不要假设它们可用来设计架构。

**构建告警策略**：项目保持 **0 条 Kotlin 警告 + 0 条 Gradle 废弃告警**。升级依赖或改构建脚本后必须用 `--warning-mode all` 验证，不要用 `-nowarn`/`@Suppress` 掩盖问题。

## 3. 目录与模块

依赖方向：`composeApp → core → webdriver / excel`。core 不得反向依赖 composeApp。

```
composeApp/   # Compose 桌面 UI（仅 jvmMain 有代码，commonMain 为空，勿把桌面代码放 commonMain）
  src/jvmMain/kotlin/tech/kotlinhero/autohelper/
    main.kt                      # Window 入口 + 未捕获异常处理
    Logging.kt                   # 全局 logger（写 ~/AuthHelper.log，超过 1MB 轮转为 .1/.2）
    App.kt                       # NavigationRail + NavHost 外壳
    ui/page/                     # TaskMode / TaskExecute / TaskHistory / Settings
    ui/route/Router.kt           # NavigationRouter 路由枚举
    ui/viewmodel/TaskExecuteViewModel.kt  # 任务分发、进度/日志/取消状态
    ui/theme/DynamicConfigTheme.kt
  src/jvmMain/composeResources/drawable/app.ico   # 打包图标
  bin/common/                    # 打包资源（chromedriver.exe、chrome-headless-shell.exe，见 bin/common/readme.md）
core/         # 业务核心（无测试源）
  src/main/kotlin/
    Task.kt / ProgressTask.kt / TaskState.kt / ImportTaskParam.kt   # 任务框架
    BrowserDriverConfig.kt / HealthSystemAuthentication.kt
    business/                    # task/、form/、contract/、diabetes/、Hypertension.kt …
    config/                      # AppPreferences（java.util.prefs 持久化）、站点 URL 常量
    excel/                       # 行模型 XxxRecord + Row → Record 映射（ExcelRowMapper.kt）
    extension/                   # Flow、Retry 等扩展
excel/        # Apache POI 封装：readExcel(path)、Row.get(oneBasedColumn) 等
webdriver/    # 自研 Selenium DSL：webDriver {} builder + WebDriver 扩展函数
```

## 4. 构建 / 运行 / 测试 / 打包

所有命令在仓库根目录（Windows PowerShell）：

```powershell
.\gradlew.bat :composeApp:run                 # 运行应用
.\gradlew.bat :composeApp:hotRunJvm           # 热重载运行（hotReloadJvmMain 用于手动推送热替换）
.\gradlew.bat build                           # 全量构建
.\gradlew.bat :core:compileKotlin :webdriver:compileKotlin :excel:compileKotlin :composeApp:compileKotlinJvm --warning-mode all   # 提交前编译自检，必须 0 告警
.\gradlew.bat :core:test :composeApp:jvmTest  # 测试（core 目前无测试源，composeApp 仅 1 个冒烟测试）
.\gradlew.bat :composeApp:packageMsi          # 打 MSI（需完整 JDK 且带 jmods，见下）
.\gradlew.bat :composeApp:createDistributable # 生成 app image
```

环境注意事项：

- **网络**：`gradle/wrapper/gradle-wrapper.properties` 指向腾讯镜像（直连 services.gradle.org 会超时）；依赖坐标由本机 `~/.gradle/init.d/oneday-mirrors.gradle` 重写到阿里云镜像（Groovy 用 `url = '...'` 写法，Gradle 9 起空格赋值已废弃）；[gradle.properties](gradle.properties) 里的 `systemProp.*.proxy*` 默认注释掉，需要代理时按本机端口自行启用。
- **配置缓存**已开启（`org.gradle.configuration-cache=true`）。构建脚本中：
  - 禁止 `project.properties["x"]`（Gradle 10 移除），改用 `providers.gradleProperty("x")`；
  - 禁止 `val task: X by tasks` 委托（Gradle 10 移除），改用 `tasks.named<X>("task") { ... }`。
- **打包**：`jlink`/`createRuntimeImage` 需要完整 JDK（含 `jmods` 目录）。当前开发机 scoop 安装的 `temurin-lts-jdk 25` 没有 jmods，jlink 会报“没有 JDK 的 jmods 模块”，属环境问题，与代码无关；换完整 JDK 即可。
- 打包配置在 [composeApp/build.gradle.kts](composeApp/build.gradle.kts)：`packageName`/`packageVersion`/`app.bind` 来自 [gradle.properties](gradle.properties) 的 `app.*`；`packageMsi` 完成后会 `start explorer` 打开产物目录（仅 Windows）。

## 5. 业务任务开发模式

新增一个“导入/自动填写”类功能时，按以下顺序落地：

1. **任务类**：在 `core/src/main/kotlin/business/<业务域>/` 下新建类，实现 `ProgressTask`（或底层 `IndependentTask` / `Task<T, R>`），`taskDescription` 用中文短语（会显示在 UI）。
2. **执行体**：`override fun execute(): Flow<TaskProgress> = flowOnDefault { ... }`；用 `params.buildWebDriver().use { }`（或 `browserDriverConfig(...).buildWebDriver()`）创建并确保关闭 WebDriver；用 `readExcel(path) { workbook -> ... }` 读取第 0 个 sheet。
3. **进度汇报**：用 `emitTotalCount(n)` / `emitProgressUpdate(n)` / `emitLog("中文日志")`；逐行处理时用 `runCatching { ... }` 隔离单条失败，失败后重新登录或重新导航（参考 `business/task/ContractImportTask.kt`）。
4. **数据模型**：在 `core/excel/` 新增 `XxxRecord` 与 `Row.toXxxRecord()`，并注册到 `ExcelRowMapper.kt`。注意 `excel` 模块的 `Row.get(column)` 是 **1-based 列号**。
5. **页面接入**：在 [TaskMode.kt](composeApp/src/jvmMain/kotlin/tech/kotlinhero/autohelper/ui/page/TaskMode.kt) 增加任务卡片（参考 `UserExcelTaskStartCard` / `TaskModeItem`），在 [TaskExecuteViewModel.kt](composeApp/src/jvmMain/kotlin/tech/kotlinhero/autohelper/ui/viewmodel/TaskExecuteViewModel.kt) 增加 `startXxxTask(params)`，统一走 `startIndexLogTask(...)`，无需自己维护进度流。
6. **配置**：账号密码、Chrome 路径等通过 `core/config/AppPreferences.kt`（`java.util.prefs`）读写；默认站点在 `HEALTH_SYSTEM_WEBSITE_URL`。新增配置项要在 `AppSettings`/相关 Config 接口与设置页同步。
7. **日志**：任务日志统一 `emitLog`；全局未捕获异常由 `main.kt` 的 logger 写入 `~/AuthHelper.log`。

### WebDriver 使用方式

```kotlin
// 低层 DSL（webdriver 模块）
webDriver { chrome { driver(driverPath); binary(browserBinaryPath); silent() } }
    .use { driver ->
        driver.loginHealthSystem(username, password)   // core/business/Common.kt
        driver.css("#pwd").sendKeys("...")
        driver.xpath("//li[text()='责任医生']").click()
    }
```

- `@WebDriverDSL` 只标注 builder 接口（`WebDriverBuilder` / `ChromeDriverBuilder`）。**不要**标注顶层扩展函数——会产生 `KT-81567` DSL marker 无效警告（历史告警已清理）。
- 常用扩展在 [WebDriverExtension.kt](webdriver/src/main/kotlin/WebDriverExtension.kt)：`css` / `xpath` / `name` / `findElement` / `findElements` / `doubleClick` / `clearSendKeys` 等；文件名为历史拼写 `WebDriverBuidler.kt`，引用或搜索时注意。

## 6. 代码风格与提交约定

- Kotlin 官方风格（`kotlin.code.style=official`），4 空格缩进；包名 `tech.kotlinhero.autohelper.*`。
- 业务解释注释用**中文**（现有代码大量中文注释），UI 文案统一中文。
- 提交信息遵循 Conventional Commits，描述用中文，例：
  - `feat(core): 添加签约服务导入功能`
  - `fix(core): 修复健康体检表和随访导入中的转诊医院逻辑`
  - `chore(build): Gradle wrapper 升级至 9.8.1`
  - 一个提交只做一件事；先 `git status` 确认没有把构建产物、驱动文件、个人配置带进来。
- **完成即提交（硬性约束）**：每完成一项工作（功能、修复、重构、文档、构建配置等）并跑完第 8 节检查清单后，**立即提交**，不允许把已完成的改动留在工作区等下一次指令；一项工作一个提交。
- 分支：`dev` 为集成分支（当前工作分支），`main` 为发布分支；功能分支命名 `feature/xxx`。存在进行中的 `feature/selenium-to-playwright`（把 Selenium 迁移到 Playwright），改动 `webdriver/` 层前先确认是否要与其统一方向。
- 不提交：`**/build/`、`.gradle`、`.idea`、`composeApp/bin/common/` 下的 chrome/chromedriver 运行时文件、个人 Excel 测试文件。

## 7. 已知陷阱

- `composeApp` 的 `commonMain` 为空：桌面专有代码全部放 `jvmMain`，Compose 资源放 `src/jvmMain/composeResources/`。
- [composeApp/build.gradle.kts](composeApp/build.gradle.kts) 的 `afterEvaluate { ... packageMsi ... }` 会用 `cmd /c start explorer` 打开产物目录，非 Windows 平台打包时需留意。
- Compose 依赖使用版本目录里的显式坐标：`commonMain` 用 `libs.compose.runtime/foundation/material3/material.icons.extended/ui/components.resources/ui.tooling.preview`；`jvmMain` 用 `libs.compose.desktop`（= `desktop-jvm-windows-x64`，含 skiko native）。插件提供的 `compose.*` 访问器自 CMP 1.10 起已废弃，不要再使用，也不要恢复旧的 `exclude(material-desktop)` 写法。
- 测试覆盖很少：`core` 测试已被上游删除，目前仅 `composeApp:jvmTest` 一个冒烟测试。新增测试请写纯单元测试，不要依赖本机桌面 Excel 文件或 chromedriver（历史上这类测试在 CI/他机必然失败）。
- 任务执行依赖本机 Chrome 与 chromedriver 路径（设置页 `Settings.kt` → `AppPreferences.chromeBinaryPath/chromeDriverPath`）；打包时驱动放在 `composeApp/bin/common/` 深层目录由程序查找。
- 无 CI：合并前必须本地跑第 8 节的检查清单。
- 构建脚本中出现 `w:` 警告即视为回归；`--warning-mode all` 下应为 0 告警（当前基线）。

## 8. Agent 工作流程（提交前检查清单）

1. 阅读本文件与目标模块现有代码，遵循上述模块边界与命名。
2. 改动后先跑编译自检（`--warning-mode all`），确认 **0 告警 0 错误**。
3. 跑 `.\gradlew.bat :core:test :composeApp:jvmTest`（有新增测试则加对应模块 test 任务）。
4. `git status` / `git diff` 自查：只提交源代码与必要文档，不带构建产物、驱动文件、个人配置。
5. **工作完成即提交（硬性约束）**：按 Conventional Commits + 中文描述提交，工作区里不留已完成的未提交改动；push 与改动 `main` 仅在用户明确要求时进行。
6. 若升级依赖或调整构建脚本：更新本文件第 2 节的版本表，并保持“0 告警”基线。

_最后更新：2026-10-17（Gradle 9.8.1 / Kotlin 2.4.21 / Compose MP 1.12.2）_
