package tech.kotlinhero.autohelper.core

interface IndexExecuteTask {
    val totalCount: Int

    fun execute(block: (index: Int) -> Unit)
}