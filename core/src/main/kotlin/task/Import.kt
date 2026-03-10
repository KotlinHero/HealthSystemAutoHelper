package tech.kotlinhero.autohelper.core.task

class SimpleRecordDescription(
    id: String,
    name: String
) : HealthRecordDescription {
    override val recordDescription: String = "$name-$id"
}

interface HealthRecordDescription {
    val recordDescription: String
}

interface HealthImportPrepare {
    suspend fun prepareImport()
}

interface HealthSingleRecordImport<in T> {
    suspend fun importRecord(record: T)
}