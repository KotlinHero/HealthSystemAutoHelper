package tech.kotlinhero.autohelper.core

interface FinishCountTask {
    fun execute(block: (Int) -> Unit)
}