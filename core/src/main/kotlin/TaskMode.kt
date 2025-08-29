package tech.kotlinhero.autohelper.core

interface TaskModeOverview {
    val description: String
}

enum class TaskMode(
    override val description: String,
) : TaskModeOverview {
    CreateHealthForm("导入健康体检表"),
    CompleteHealthForm("完善健康体检表"),
}