package tech.kotlinhero.autohelper.ui.task

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import tech.kotlinhero.autohelper.core.ProgressTask
import tech.kotlinhero.autohelper.core.browserDriverConfig
import tech.kotlinhero.autohelper.core.business.diabetes.risk.DiabetesRiskStratificationTask
import tech.kotlinhero.autohelper.core.business.form.HealthFormCompleteTask
import tech.kotlinhero.autohelper.core.business.form.HealthFormCreateTask
import tech.kotlinhero.autohelper.core.business.task.ContractImportTask
import tech.kotlinhero.autohelper.core.business.task.ContractSaveTask
import tech.kotlinhero.autohelper.core.business.task.DiabetesVisitImportTask
import tech.kotlinhero.autohelper.core.business.task.HypertensionRiskStratificationTask
import tech.kotlinhero.autohelper.core.business.task.HypertensionVisitImportTask
import tech.kotlinhero.autohelper.core.config.AppPreferences
import tech.kotlinhero.autohelper.core.healthSystemAuthentication
import tech.kotlinhero.autohelper.core.importTaskParam
import tech.kotlinhero.autohelper.ui.theme.TaskDomain
import tech.kotlinhero.autohelper.ui.viewmodel.UserExcelTaskStartParams

/**
 * 一组系统账号。8 个任务共用 3 组账号：高血压两项共用高血压账号，
 * 糖尿病两项与两个健康体检任务共用糖尿病账号（业务侧约定），签约两项共用签约账号。
 */
enum class AccountGroup(val label: String) {
    Hypertension("高血压账号"),
    Diabetes("糖尿病账号"),
    Contract("签约账号"),
}

data class TaskCredentials(val username: String, val password: String)

object TaskCredentialsStore {

    fun load(group: AccountGroup): TaskCredentials = when (group) {
        AccountGroup.Hypertension -> TaskCredentials(AppPreferences.hyperVisitUsername, AppPreferences.hyperVisitPassword)
        AccountGroup.Diabetes -> TaskCredentials(AppPreferences.diabetesUsername, AppPreferences.diabetesPassword)
        AccountGroup.Contract -> TaskCredentials(AppPreferences.contractUsername, AppPreferences.contractPassword)
    }

    fun save(group: AccountGroup, username: String, password: String) {
        when (group) {
            AccountGroup.Hypertension -> {
                AppPreferences.hyperVisitUsername = username
                AppPreferences.hyperVisitPassword = password
            }
            AccountGroup.Diabetes -> {
                AppPreferences.diabetesUsername = username
                AppPreferences.diabetesPassword = password
            }
            AccountGroup.Contract -> {
                AppPreferences.contractUsername = username
                AppPreferences.contractPassword = password
            }
        }
    }
}

/**
 * 一个可启动的任务：任务台卡片、抽屉标题、运行记录都以这份描述为准。
 * create 只负责构造 core 里的 ProgressTask，进度与日志由 TaskExecuteViewModel 统一接管。
 */
class TaskDefinition(
    val id: String,
    val title: String,
    val description: String,
    val domain: TaskDomain,
    val icon: ImageVector,
    val accountGroup: AccountGroup,
    internal val create: (UserExcelTaskStartParams) -> ProgressTask,
)

class TaskGroup(
    val title: String,
    val domain: TaskDomain,
    val tasks: List<TaskDefinition>,
)

object TaskCatalog {

    private fun importParam(username: String, password: String, excelPath: String) = importTaskParam(
        AppPreferences.chromeBinaryPath,
        AppPreferences.chromeDriverPath,
        username,
        password,
        excelPath,
    )

    private fun driverConfig() = browserDriverConfig(AppPreferences.chromeBinaryPath, AppPreferences.chromeDriverPath)

    val all: List<TaskDefinition> = listOf(
        // 高血压患者管理
        TaskDefinition(
            id = "hyper-visit",
            title = "导入高血压随访",
            description = "把随访记录按行录入，逐条保存",
            domain = TaskDomain.Hypertension,
            icon = Icons.Outlined.MonitorHeart,
            accountGroup = AccountGroup.Hypertension,
            create = { p -> HypertensionVisitImportTask(importParam(p.username, p.password, p.excelPath)) },
        ),
        TaskDefinition(
            id = "hyper-group",
            title = "导入高血压分组评估",
            description = "按风险分层结果更新患者分组",
            domain = TaskDomain.Hypertension,
            icon = Icons.Outlined.Layers,
            accountGroup = AccountGroup.Hypertension,
            create = { p ->
                HypertensionRiskStratificationTask(
                    driverConfig(),
                    healthSystemAuthentication(p.username, p.password),
                    p.excelPath,
                )
            },
        ),
        // 糖尿病患者管理
        TaskDefinition(
            id = "diabetes-visit",
            title = "导入糖尿病随访",
            description = "把随访记录按行录入，逐条保存",
            domain = TaskDomain.Diabetes,
            icon = Icons.Outlined.WaterDrop,
            accountGroup = AccountGroup.Diabetes,
            create = { p -> DiabetesVisitImportTask(importParam(p.username, p.password, p.excelPath)) },
        ),
        TaskDefinition(
            id = "diabetes-group",
            title = "导入糖尿病分组评估",
            description = "按风险分层结果更新患者分组",
            domain = TaskDomain.Diabetes,
            icon = Icons.Outlined.Layers,
            accountGroup = AccountGroup.Diabetes,
            create = { p ->
                DiabetesRiskStratificationTask(
                    driverConfig(),
                    healthSystemAuthentication(p.username, p.password),
                    p.excelPath,
                )
            },
        ),
        // 健康体检表
        TaskDefinition(
            id = "exam-new",
            title = "新建健康体检表",
            description = "给还没有体检档案的居民建表",
            domain = TaskDomain.HealthForm,
            icon = Icons.AutoMirrored.Outlined.Assignment,
            accountGroup = AccountGroup.Diabetes,
            create = { p ->
                HealthFormCreateTask(
                    driverConfig(),
                    healthSystemAuthentication(p.username, p.password),
                    p.excelPath,
                )
            },
        ),
        TaskDefinition(
            id = "exam-fill",
            title = "完善健康体检表",
            description = "补齐已有体检表里缺失的项",
            domain = TaskDomain.HealthForm,
            icon = Icons.AutoMirrored.Outlined.FactCheck,
            accountGroup = AccountGroup.Diabetes,
            create = { p ->
                HealthFormCompleteTask(
                    driverConfig(),
                    healthSystemAuthentication(p.username, p.password),
                    p.excelPath,
                )
            },
        ),
        // 签约服务
        TaskDefinition(
            id = "contract-import",
            title = "导入签约服务",
            description = "按名单批量建立签约关系",
            domain = TaskDomain.Contract,
            icon = Icons.Outlined.Handshake,
            accountGroup = AccountGroup.Contract,
            create = { p -> ContractImportTask(importParam(p.username, p.password, p.excelPath)) },
        ),
        TaskDefinition(
            id = "archive-save",
            title = "档案保存",
            description = "把编辑中的居民档案逐个保存",
            domain = TaskDomain.Contract,
            icon = Icons.Outlined.Save,
            accountGroup = AccountGroup.Contract,
            create = { p -> ContractSaveTask(importParam(p.username, p.password, p.excelPath)) },
        ),
    )

    /** 任务台按 2 列布局：左列高血压 + 健康体检表，右列糖尿病 + 签约服务 */
    val groups: List<TaskGroup> = listOf(
        TaskGroup("高血压患者管理", TaskDomain.Hypertension, all.filter { it.domain == TaskDomain.Hypertension }),
        TaskGroup("糖尿病患者管理", TaskDomain.Diabetes, all.filter { it.domain == TaskDomain.Diabetes }),
        TaskGroup("健康体检表", TaskDomain.HealthForm, all.filter { it.domain == TaskDomain.HealthForm }),
        TaskGroup("签约服务", TaskDomain.Contract, all.filter { it.domain == TaskDomain.Contract }),
    )

    /** 左列两组的顺序（高血压、健康体检表），右列两条（糖尿病、签约服务） */
    val leftColumn: List<TaskGroup> = listOf(groups[0], groups[2])
    val rightColumn: List<TaskGroup> = listOf(groups[1], groups[3])

    fun byId(id: String): TaskDefinition? = all.firstOrNull { it.id == id }
}