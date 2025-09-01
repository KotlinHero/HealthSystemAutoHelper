package tech.kotlinhero.autohelper.core.excel

import org.apache.poi.ss.usermodel.Row
import tech.kotlinhero.autohelper.excel.get

data class HypertensionVisitRecord(
    val name: String,
    val id: String,
    val planDate: String,
    val visitDate: String,
    val visitWay: VisitWay,
    val visitNature: VisitNature,
    val currentSymptom: CurrentSymptom
)

internal fun Row.toHypertensionVisitRecord(): HypertensionVisitRecord {
    return HypertensionVisitRecord(
        name = this[1],
        id = this[2],
        planDate = this[11],
        visitDate = this[12],
        visitWay = VisitWay(this[13]),
        visitNature = VisitNature(this[14]),
        currentSymptom = CurrentSymptom(this[15])
    )
}

@JvmInline
value class VisitWay(val value: String) {
    fun toOption() {

    }
}

@JvmInline
value class VisitNature(val value: String) {
    fun toOption() {

    }
}

@JvmInline
value class CurrentSymptom(val value: String) {
    fun toOption() {

    }
}