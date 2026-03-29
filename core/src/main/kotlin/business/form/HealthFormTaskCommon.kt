package tech.kotlinhero.autohelper.core.business.form

import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.webdriver.*

interface HealthForm {
    val checkDate: String
    val name: String
    val id: String
    val isElder: Boolean
    val isHypertension: Boolean
    val isDiabetes: Boolean
    val heartRate: String
    val temperature: String
    val breathRate: String
    val rightConstriction: String
    val rightDiastolic: String
    val leftConstriction: String
    val leftDiastolic: String
    val height: String
    val weight: String
    val waistline: String
    val leftEye: String
    val rightEye: String
    val fastingBloodGlucose: String
    val medicineUse1: String
    val medicine1: String
    val medicineUseDate1: String
    val medicineEachDose1: String
    val medicineYield1: MedicineYield
    val medicineUse2: String
    val medicine2: String
    val medicineUseDate2: String
    val medicineEachDose2: String
    val medicineYield2: MedicineYield
    val medicineUse3: String
    val medicine3: String
    val medicineUseDate3: String
    val medicineEachDose3: String
    val medicineYield3: MedicineYield
    val medicineUse4: String
    val medicine4: String
    val medicineUseDate4: String
    val medicineEachDose4: String
    val medicineYield4: MedicineYield
    val hasAbnormal: Boolean
    val abnormality1: String
    val isPutIntoAdministration: Boolean
    val isSuggestedReview: Boolean
    val isSuggestReferral: Boolean
    val isNeedQuitSmoking: Boolean
    val isNeedHealthDrinking: Boolean
    val isNeedDiet: Boolean
    val isNeedExercise: Boolean
    val isNeedLostWeight: Boolean
    val targetWeight: String
    val hasOther: Boolean
    val pjOther: String
}

val HealthForm.manas: List<String>
    get() = listOf(
        isPutIntoAdministration to "1",
        isSuggestedReview to "2",
        isSuggestReferral to "3",
    ).asSequence().filter { it.first }.map { it.second }.toList()

@JvmInline
value class MedicineYield(val value: String) {
    fun option(): String? {
        return when (value) {
            "规律" -> "1"
            "间断" -> "2"
            "不服药" -> "3"
            else -> null
        }
    }
}

val HealthForm.otherDiseases: String
    get() = listOf(
        isHypertension to "高血压",
        isDiabetes to "糖尿病"
    ).filter { it.first }.joinToString(",") { it.second }

val HealthForm.checkWays: List<String>
    get() = listOfNotNull(
        if (isElder) "2" else null,
        if (isHypertension) "3" else null,
        if (isDiabetes) "4" else null
    )

internal fun WebDriver.selectOptionWhenAllNoSelected(nameValuePair: Pair<String, String>) {
    findElements { name(nameValuePair.first) }.none {
        it.isSelected
    }.let {
        if (it) {
            selectWhenNotSelect(nameValuePair)
        }
    }
}

internal fun WebDriver.sendKeysWhenInputEmpty(nameValuePair: Pair<String, String>) {
    name(nameValuePair.first).sendKeysWhenInputEmpty(nameValuePair.second)
}

internal fun WebDriver.sendKeysWhenValueNotEmpty(nameValuePair: Pair<String, String>) {
    name(nameValuePair.first).sendKeysWhenValueNotEmpty(nameValuePair.second)
}

internal fun WebDriver.selectWhenNotSelect(nameValuePair: Pair<String, String>) {
    xpathByNameWithValue(nameValuePair.first, nameValuePair.second).selectWhenNotSelected()
}

/**
 * 36.6-37.2随机
 */
internal fun randomTemperature(): String = (36.5 + Math.random() * 0.7).let { "%.1f".format(it) }

/**
 * 60-100随机
 */
internal fun randomBreathRate(): String = (60 + Math.random() * 40).toInt().toString()

/**
 * 4.5-4.8随机
 */
internal fun randomEyeSight(): String = (4.5 + Math.random() * 0.3).let { "%.1f".format(it) }

context(driver: WebDriver)
internal fun gotoHealthFormListPage() = driver.run {
    id("HR").click()
    id("WL_module_D20").click()
    allByCss("img.x-form-trigger.x-form-arrow-trigger").getOrNull(0)?.click()
    css("html > body > div:nth-of-type(8) > div > div:nth-of-type(6)").click()
}

context(driver: WebDriver)
internal fun getMedicineElementSuffix(): String {
    val prefix = "medicine_1_"
    return driver.findElements {
        xpath("//*[contains(@name, '${prefix}')]")
    }.first().getAttribute("name")?.let {
        it.substring(prefix.length, it.length)
    } ?: ""
}