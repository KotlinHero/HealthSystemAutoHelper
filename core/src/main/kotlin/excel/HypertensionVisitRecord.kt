package tech.kotlinhero.autohelper.core.excel

data class HypertensionVisitRecord(
    val name: String,
    val id: String,
    val planDate: String,
    val visitDate: String,
    val visitWay: String,
    val visitNature: String,
    val currentSymptom: String
)