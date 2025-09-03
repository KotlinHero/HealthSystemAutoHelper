package tech.kotlinhero.autohelper.core

enum class TaskState(
    val description: String
) {
    Running("运行中"),
    FINISHED("结束"),
    Cancelling("取消中"),
}