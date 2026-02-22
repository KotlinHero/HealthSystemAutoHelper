package tech.kotlinhero.autohelper.core

interface Task<T, R> {
    fun execute(context: T): R
}

interface IndependentTask<R> : Task<Unit, R> {
    override fun execute(context: Unit): R = execute()

    fun execute(): R
}

interface TaskDescription {
    val taskDescription: String
}