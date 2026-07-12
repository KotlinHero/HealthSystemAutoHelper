package tech.kotlinhero.autohelper.core.excel

import org.apache.poi.ss.usermodel.Row
import tech.kotlinhero.autohelper.core.task.HealthRecordDescription
import tech.kotlinhero.autohelper.core.task.SimpleRecordDescription
import tech.kotlinhero.autohelper.excel.get

data class ContractRecord(
    val name: String,
    val id: String,
    val type: String,
    val date: String
) : HealthRecordDescription by SimpleRecordDescription(id, name)

fun Row.toContractRecord(): ContractRecord = ContractRecord(
    name = this[2],
    id = this[3],
    type = this[4],
    date = this[5]
)
