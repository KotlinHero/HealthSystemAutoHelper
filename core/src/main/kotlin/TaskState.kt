package tech.kotlinhero.autohelper.core

enum class TaskState(
    val description: String,
    val isActive: Boolean = false,
) {
    Idle("空闲"),
    Running("运行中", isActive = true),
    Cancelling("取消中", isActive = true),
    Finished("结束"),
    Failed("失败"),
    Cancelled("已取消"),
}
