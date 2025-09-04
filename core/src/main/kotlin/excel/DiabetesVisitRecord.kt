package tech.kotlinhero.autohelper.core.excel

import org.apache.poi.ss.usermodel.Row
import tech.kotlinhero.autohelper.excel.get

class DiabetesVisitRecord(
    val name: String,
    val id: String,
    val planDate: String,
    val visitDate: String,
    val visitWay: DiabetesVisitWay,
    val visitNature: String,
    val currentSymptom: Symptoms,
    val constriction: String,
    val diastolic: String,
    val weight: String,
    val targetWeight: String,
    val bloodGlucose: String,
    val smokeCount: String,
    val targetSmokeCount: String,
    val drinkCount: String,
    val targetDrinkCount: String,
    val trainTimesWeek: String,
    val trainMinute: String,
    val targetTrainTimesWeek: String,
    val targetTrainMinute: String,
    val otherSigns: String,
    val food: String,
    val targetFood: String,
    val medicine: Medicine,
    val visitType: VisitType,
    val referralReason: String,
    val agencyAndDept: String,
    val needDoubleVisit: NeedDoubleVisit
)

@JvmInline
value class Symptoms(val value: String) {
    fun toOption(): Int = when (value) {
        "无症状" -> 1
        "多饮" -> 2
        "多食" -> 3
        "多尿" -> 4
        "视力模糊" -> 5
        "感染" -> 6
        "手脚麻木" -> 7
        "下肢浮肿" -> 8
        "体重明显下降" -> 9
        else -> throw IllegalArgumentException("未知症状: $value")
    }
}

@JvmInline
value class DiabetesVisitWay(val value: String) {
    fun toOption(): Int = when (value) {
        "门诊随访" -> 1
        "上门随访" -> 4
        "电话随访" -> 5
        else -> throw IllegalArgumentException("未知随访方式: $value")
    }
}

@JvmInline
value class VisitType(val value: String) {
    fun toOption(): Int = when (value) {
        "控制满意" -> 1
        "控制不满意" -> 2
        "不良反应" -> 3
        "并发症" -> 4
        "有新并发症" -> 5
        "原有并发症加重" -> 6
        "无并发症" -> 7
        else -> throw IllegalArgumentException("未知随访分类: $value")
    }
}

internal fun Row.toDiabetesVisitRecord(): DiabetesVisitRecord {
    return DiabetesVisitRecord(
        name = this[1],
        id = this[2],
        planDate = this[11],
        visitDate = this[12],
        visitWay = DiabetesVisitWay(this[13]),
        visitNature = this[14],
        currentSymptom = Symptoms(this[15]),
        constriction = this[16],
        diastolic = this[17],
        weight = this[18],
        targetWeight = this[19],
        bloodGlucose = this[23],
        smokeCount = this[24],
        targetSmokeCount = this[25],
        drinkCount = this[26],
        targetDrinkCount = this[27],
        trainTimesWeek = this[28],
        trainMinute = this[29],
        targetTrainTimesWeek = this[30],
        targetTrainMinute = this[31],
        otherSigns = this[32],
        food = this[33],
        targetFood = this[34],
        medicine = Medicine(this[37]),
        visitType = VisitType(this[38]),
        referralReason = this[40],
        agencyAndDept = this[41],
        needDoubleVisit = NeedDoubleVisit(this[42])
    )
}