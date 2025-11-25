package tech.kotlinhero.autohelper.core.excel

import org.apache.poi.ss.usermodel.Row
import tech.kotlinhero.autohelper.excel.get
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HypertensionVisitRecord(
    val name: String,
    val id: String,
    val planDate: String,
    val visitDate: String,
    val visitWay: HyperVisitWay,
    val visitNature: String,
    val currentSymptom: CurrentSymptom,
    val constriction: String,
    val diastolic: String,
    val weight: String,
    val targetWeight: String,
    val heartRate: String,
    val otherSigns: String,
    val smokeCount: String,
    val targetSmokeCount: String,
    val drinkCount: String,
    val targetDrinkCount: String,
    val trainTimesWeek: String,
    val trainMinute: String,
    val targetTrainTimesWeek: String,
    val targetTrainMinute: String,
    val salt: Salt,
    val targetSalt: Salt,
    val psychologyChange: PsychologyChange,
    val obeyDoctor: ObeyDoctor,
    val medicine: Medicine,
    val visitEvaluate: VisitEvaluate,
    val referralReason: String,
    val agencyAndDept: String,
    val needDoubleVisit: NeedDoubleVisit
)

internal fun Row.toHypertensionVisitRecord(): HypertensionVisitRecord {
    return HypertensionVisitRecord(
        name = this[1],
        id = this[2],
        planDate = this[11],
        visitDate = this[12],
        visitWay = HyperVisitWay(this[13]),
        visitNature = this[14],
        currentSymptom = CurrentSymptom(this[15]),
        constriction = this[16],
        diastolic = this[17],
        weight = this[18],
        targetWeight = this[19],
        heartRate = this[22],
        otherSigns = this[23].takeIf { it.isNotEmpty() }?.let { "血糖:${it}" } ?: "",
        smokeCount = this[24],
        targetSmokeCount = this[25],
        drinkCount = this[26],
        targetDrinkCount = this[27],
        trainTimesWeek = this[28],
        trainMinute = this[29],
        targetTrainTimesWeek = this[30],
        targetTrainMinute = this[31],
        salt = Salt(this[32]),
        targetSalt = Salt(this[33]),
        psychologyChange = PsychologyChange(this[34]),
        obeyDoctor = ObeyDoctor(this[35]),
        medicine = Medicine(this[37]),
        visitEvaluate = VisitEvaluate(this[38]),
        referralReason = this[40],
        agencyAndDept = this[41],
        needDoubleVisit = NeedDoubleVisit(this[42]),
    )
}

@JvmInline
value class NeedDoubleVisit(val value: String) {
    fun toOption(): Int = when (value) {
        "是" -> 1
        else -> 2
    }
}

@JvmInline
value class VisitEvaluate(val value: String) {
    fun toOption(): Int = when (value) {
        "控制满意" -> 1
        "控制不满意" -> 2
        "不良反应" -> 3
        "并发症" -> 4
        else -> throw IllegalArgumentException("未知随访分类: $value")
    }
}

@JvmInline
value class Medicine(val value: String) {
    fun toOption(): Int = when (value) {
        "规律" -> 1
        "间断" -> 2
        "不服药" -> 3
        else -> throw IllegalArgumentException("未知服药依从性: $value")
    }
}

@JvmInline
value class ObeyDoctor(val value: String) {
    fun toOption(): Int = when (value) {
        "良好" -> 1
        "一般" -> 2
        "差" -> 3
        else -> throw IllegalArgumentException("未知遵医行为: $value")
    }
}

@JvmInline
value class PsychologyChange(val value: String) {
    fun toOption(): Int = when (value) {
        "良好" -> 1
        "一般" -> 2
        "差" -> 3
        else -> throw IllegalArgumentException("未知心理调整: $value")
    }
}

@JvmInline
value class Salt(val value: String) {
    fun toOption(): Int = when (value) {
        "轻" -> 1
        "中" -> 2
        "重" -> 2
        else -> throw IllegalArgumentException("未知摄盐: $value")
    }
}

@JvmInline
value class HyperVisitWay(val value: String) {
    fun toOption(): Int = when (value) {
        "门诊随访" -> 1
        "家庭随访" -> 2
        "电话随访" -> 3
        else -> throw IllegalArgumentException("未知随访方式: $value")
    }
}

@JvmInline
value class CurrentSymptom(val value: String) {
    fun toOption(): Int = when (value) {
        "无症状" -> 1
        "头痛头晕" -> 2
        "恶心呕吐" -> 3
        "眼花耳鸣" -> 4
        "呼吸苦难" -> 5
        "心悸胸闷" -> 6
        "鼻衄出血不止" -> 7
        "四肢发麻" -> 8
        "下肢水肿" -> 9
        else -> throw IllegalArgumentException("未知症状: $value")
    }
}

val HypertensionVisitRecord.nextVisitDate: String
    get() {
        if (visitDate.isEmpty()) {
            return ""
        }
        return LocalDate.parse(visitDate)
            .plusDays(14)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }