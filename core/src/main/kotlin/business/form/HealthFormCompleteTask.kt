package tech.kotlinhero.autohelper.core.business.form

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import org.apache.poi.ss.usermodel.Row
import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.core.*
import tech.kotlinhero.autohelper.core.business.loginHealthSystem
import tech.kotlinhero.autohelper.core.excel.ExcelRowMapper
import tech.kotlinhero.autohelper.core.task.HealthExcelListProgressTask
import tech.kotlinhero.autohelper.core.task.HealthRecordDescription
import tech.kotlinhero.autohelper.core.task.HealthRecordImporter
import tech.kotlinhero.autohelper.core.task.SimpleRecordDescription
import tech.kotlinhero.autohelper.excel.get
import tech.kotlinhero.autohelper.webdriver.*

class HealthFormCompleteTask(
    private val browserDriverConfig: BrowserDriverConfig,
    private val healthSystemAuthentication: HealthSystemAuthentication,
    private val excelFilePath: String
) : ProgressTask {

    override val taskDescription: String = "完善体检表任务"

    override fun execute(): Flow<TaskProgress> {
        val driver = browserDriverConfig.buildWebDriver()
        return HealthExcelListProgressTask(
            driver = driver,
            importer = HealthFormCompleteImporter(
                driver = driver,
                authentication = healthSystemAuthentication
            ),
            excelRowMapper = HealthFormExcelRowMapper(),
            excelFilePath = excelFilePath
        ).execute().onCompletion {
            driver.quit()
        }
    }
}

private class HealthFormCompleteImporter(
    private val driver: WebDriver,
    private val authentication: HealthSystemAuthentication
) : HealthRecordImporter<HealthFormRecord> {

    override fun prepareImport() = driver.run {
        loginHealthSystem(authentication)
        gotoHealthFormListPage()
    }

    private fun gotoHealthFormListPage() = driver.run {
        id("HR").click()
        id("WL_module_D20").click()
        allByCss("img.x-form-trigger.x-form-arrow-trigger").getOrNull(0)?.click()
        css("html > body > div:nth-of-type(8) > div > div:nth-of-type(6)").click()
    }

    override suspend fun importRecord(record: HealthFormRecord): Unit = driver.run {
        name("idCard").clearSendKeys(record.id)
        css("button.x-btn-text.query").click()
        doubleClick {
            css("table[class='x-grid3-row-table']")
        }
        delay(1000)
        findElements {
            xpath("//div[@class='x-grid3-cell-inner x-grid3-col-0']")
        }.firstOrNull {
            it.text == record.checkDate
        }?.click() ?: throw IllegalArgumentException()

        delay(1500)
        record.checkWays.forEach { checkWay ->
            selectWhenNotSelect("checkWay" to checkWay)
        }
        selectWhenNotSelect("symptom" to "01")
        listOf(
            "temperature" to record.temperature,
            "breathe" to record.breathRate
        ).forEach { sendKeysWhenInputEmpty(it) }

        listOf(
            "pulse" to record.heartRate,
            "constriction" to record.rightConstriction,
            "diastolic" to record.rightDiastolic,
            "constriction_L" to record.leftConstriction,
            "diastolic_L" to record.leftDiastolic,
            "height" to record.height,
            "weight" to record.weight,
            "waistline" to record.waistline
        ).forEach { sendKeysWhenValueNotEmpty(it) }

        if (record.isElder) {
            listOf(
                "healthStatus" to "1",
                "selfCare" to "1"
            ).forEach { selectWhenNotSelect(it) }
        }

        listOf(
            "physicalExerciseFrequency" to "4",
            "dietaryHabit" to "1",
            "wehtherSmoke" to "1",
            "drinkingFrequency" to "1",
            "occupational" to "1",
            "lip" to "1",
            "denture" to "1",
            "pharyngeal" to "1"
        ).forEach { selectOptionWhenAllNoSelected(it) }

        listOf(
            "leftEye" to record.leftEye,
            "rightEye" to record.rightEye
        ).forEach { sendKeysWhenInputEmpty(it) }

        listOf(
            "hearing" to "1",
            "motion" to "1",
            "skin" to "1",
            "sclera" to "1",
            "lymphnodes" to "1",
            "barrelChest" to "1",
            "breathSound" to "1",
            "rales" to "1",
        ).forEach { selectOptionWhenAllNoSelected(it) }

        sendKeysWhenValueNotEmpty("heartRate" to record.heartRate)
        listOf(
            "rhythm" to "1",
            "heartMurmur" to "1"
        ).forEach { selectOptionWhenAllNoSelected(it) }

        listOf(
            "abdominAltend" to "1",
            "adbominAlmass" to "1",
            "liverBig" to "1",
            "splenomegaly" to "1",
            "dullness" to "1",
            "edema" to "1"
        ).forEach { selectOptionWhenAllNoSelected(it) }

        if (record.isDiabetes) {
            selectWhenNotSelect("footPulse" to "2")
        }

        listOf(
            "hgb" to record.hemoglobin,
            "wbc" to record.whiteBloodCell,
            "platelet" to record.platelet
        ).forEach { sendKeysWhenValueNotEmpty(it) }

        sendKeysWhenInputEmpty("fbs" to record.fastingBloodGlucose)

        selectWhenNotSelect("ecg" to if (record.isEcgNormal) "1" else "2")

        listOf(
            "alt" to record.serumAlanineAminotransferase,
            "ast" to record.serumGlutamicOxalaceticTransaminase,
            "tbil" to record.totalBilirubin,
            "cr" to record.serumCreatinine,
            "bun" to record.bloodUreaNitrogen,
            "tc" to record.totalCholesterol,
            "tg" to record.triglyceride,
            "ldl" to record.serumLowDensityLipoproteinCholesterol,
            "hdl" to record.serumHighDensityLipoproteinCholesterol
        ).forEach { sendKeysWhenValueNotEmpty(it) }

        if (record.chestXray.isNotEmpty()) {
            selectWhenNotSelect("x" to if (record.chestXray == "正常") "1" else "2")
        }
        if (record.bulTrasonic.isNotEmpty()) {
            selectWhenNotSelect("b" to if (record.bulTrasonic == "正常") "1" else "2")
        }

        listOf(
            "cerebrovascularDiseases" to "1",
            "kidneyDiseases" to "1",
            "heartDisease" to "1",
            "VascularDisease" to "1",
            "eyeDiseases" to "1",
            "neurologicalDiseases" to "1"
        ).forEach { selectOptionWhenAllNoSelected(it) }

        val otherDiseases = record.otherDiseases
        if (otherDiseases.isNotEmpty()) {
            selectWhenNotSelect("otherDiseasesone" to "2")
            sendKeysWhenValueNotEmpty("otherDiseasesoneDesc" to otherDiseases)
        } else {
            selectWhenNotSelect("otherDiseasesone" to "1")
        }

        delay(500)

        listOf(
            "inhospitalFlag" to "n",
            "infamilybedFlag" to "n",
            "medicineFlag" to record.medicineFlag
        ).forEach { selectWhenNotSelect(it) }

        val medicineElementSuffix = getMedicineElementSuffix()
        listOf(
            "medicine_1_${medicineElementSuffix}" to record.medicine1,
            "use_1" to record.medicineUse1,
            "useDate_1" to record.medicineUseDate1,
            "eachDose_1" to record.medicineEachDose1,
            "medicine_2_${medicineElementSuffix}" to record.medicine2,
            "use_2" to record.medicineUse2,
            "useDate_2" to record.medicineUseDate2,
            "eachDose_2" to record.medicineEachDose2,
            "medicine_3_${medicineElementSuffix}" to record.medicine3,
            "use_3" to record.medicineUse3,
            "useDate_3" to record.medicineUseDate3,
            "eachDose_3" to record.medicineEachDose3,
            "medicine_4_${medicineElementSuffix}" to record.medicine4,
            "use_4" to record.medicineUse4,
            "useDate_4" to record.medicineUseDate4,
            "eachDose_4" to record.medicineEachDose4,
        ).forEach { sendKeysWhenValueNotEmpty(it) }

        listOf(
            "medicineYield1" to record.medicineYield1,
            "medicineYield2" to record.medicineYield2,
            "medicineYield3" to record.medicineYield3,
            "medicineYield4" to record.medicineYield4,
        ).forEach { pair ->
            pair.second.option()?.let { option ->
                selectWhenNotSelect(pair.first to option)
            }
        }

        selectWhenNotSelect("nonimmuneFlag" to "n")

        if (record.hasAbnormal) {
            selectWhenNotSelect("abnormality" to "2")
            listOf(
                "abnormality1" to record.abnormality1,
                "abnormality2" to record.abnormality2,
                "abnormality3" to record.abnormality3,
                "abnormality4" to record.abnormality4
            ).forEach {
                sendKeysWhenValueNotEmpty(it)
            }
        } else {
            selectWhenNotSelect("abnormality" to "1")
        }

        record.manas.forEach {
            selectWhenNotSelect("mana" to it)
        }

        record.riskControls.forEach {
            selectWhenNotSelect("riskfactorsControl" to it)
        }
        if (record.isNeedLostWeight) {
            sendKeysWhenValueNotEmpty("targetWeight" to record.targetWeight)
        }
        if (record.isSuggestVaccination) {
            sendKeysWhenValueNotEmpty("vaccine" to record.vaccine)
        }
        if (record.hasOther) {
            sendKeysWhenValueNotEmpty("pjOther" to record.pjOther)
        }

        firstByXpath("//*[text() = '确定(F1)']")?.click()
        delay(500)
        css("button[id='CLOSE']").click()
    }

    private fun WebDriver.getMedicineElementSuffix(): String {
        val prefix = "medicine_1_"
        return findElements {
            xpath("//*[contains(@name, '${prefix}')]")
        }.first().getAttribute("name")?.let {
            it.substring(prefix.length, it.length)
        } ?: ""
    }
}

private data class HealthFormRecord(
    val checkDate: String,
    val name: String,
    val id: String,
    val isElder: Boolean,
    val isHypertension: Boolean,
    val isDiabetes: Boolean,
    val heartRate: String,
    val temperature: String,
    val breathRate: String,
    val rightConstriction: String,
    val rightDiastolic: String,
    val leftConstriction: String,
    val leftDiastolic: String,
    val height: String,
    val weight: String,
    val waistline: String,
    val leftEye: String,
    val rightEye: String,
    val hemoglobin: String,
    val whiteBloodCell: String,
    val platelet: String,
    val fastingBloodGlucose: String,
    val isEcgNormal: Boolean,
    val serumAlanineAminotransferase: String,
    val serumGlutamicOxalaceticTransaminase: String,
    val totalBilirubin: String,
    val serumCreatinine: String,
    val bloodUreaNitrogen: String,
    val totalCholesterol: String,
    val triglyceride: String,
    val serumLowDensityLipoproteinCholesterol: String,
    val serumHighDensityLipoproteinCholesterol: String,
    val chestXray: String,
    val bulTrasonic: String,
    val medicineUse1: String,
    val medicine1: String,
    val medicineUseDate1: String,
    val medicineEachDose1: String,
    val medicineYield1: MedicineYield,
    val medicineUse2: String,
    val medicine2: String,
    val medicineUseDate2: String,
    val medicineEachDose2: String,
    val medicineYield2: MedicineYield,
    val medicineUse3: String,
    val medicine3: String,
    val medicineUseDate3: String,
    val medicineEachDose3: String,
    val medicineYield3: MedicineYield,
    val medicineUse4: String,
    val medicine4: String,
    val medicineUseDate4: String,
    val medicineEachDose4: String,
    val medicineYield4: MedicineYield,
    val hasAbnormal: Boolean,
    val abnormality1: String,
    val abnormality2: String,
    val abnormality3: String,
    val abnormality4: String,
    val isPutIntoAdministration: Boolean,
    val isSuggestedReview: Boolean,
    val isSuggestReferral: Boolean,
    val isNeedQuitSmoking: Boolean,
    val isNeedHealthDrinking: Boolean,
    val isNeedDiet: Boolean,
    val isNeedExercise: Boolean,
    val isNeedLostWeight: Boolean,
    val targetWeight: String,
    val isSuggestVaccination: Boolean,
    val vaccine: String,
    val hasOther: Boolean,
    val pjOther: String,
) : HealthRecordDescription by SimpleRecordDescription(id, name)

private val HealthFormRecord.checkWays: List<String>
    get() = listOfNotNull(
        if (isElder) "2" else null,
        if (isHypertension) "3" else null,
        if (isDiabetes) "4" else null
    )

private val HealthFormRecord.otherDiseases: String
    get() = listOf(
        isHypertension to "高血压",
        isDiabetes to "糖尿病"
    ).filter { it.first }.joinToString(",")

private val HealthFormRecord.medicineFlag: String
    get() = if (medicineUse1.isEmpty()) "n" else "y"

@JvmInline
private value class MedicineYield(val value: String) {
    fun option(): String? {
        return when (value) {
            "规律" -> "1"
            "间断" -> "2"
            "不服药" -> "3"
            else -> null
        }
    }
}

private val HealthFormRecord.manas: List<String>
    get() = listOf(
        isPutIntoAdministration to "1",
        isSuggestedReview to "2",
        isSuggestReferral to "3",
    ).asSequence().filter { it.first }.map { it.second }.toList()

private val HealthFormRecord.riskControls: List<String>
    get() = listOf(
        isNeedQuitSmoking to "1",
        isNeedHealthDrinking to "2",
        isNeedDiet to "3",
        isNeedExercise to "4",
        isNeedLostWeight to "5",
        isSuggestVaccination to "6",
        hasOther to "7"
    ).asSequence().filter { it.first }.map { it.second }.toList()

//private fun formatDateToYMD(input: String): String {
//    val timeFormatter = DateTimeFormatterBuilder()
//        .appendPattern("M/d/")
//        .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
//        .toFormatter()
//    val date = LocalDate.parse(input, timeFormatter)
//    return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
//}

private class HealthFormExcelRowMapper : ExcelRowMapper<HealthFormRecord> {
    override val dropCount: Int = 2
    override val sheetIndex: Int = 0

    override fun mapRowTo(row: Row): HealthFormRecord {
        return HealthFormRecord(
            checkDate = row[1],
            name = row[2],
            id = row[6],
            isElder = row[7].isNotEmpty(),
            isHypertension = row[8].isNotEmpty(),
            isDiabetes = row[9].isNotEmpty(),
            heartRate = row[40],
            temperature = randomTemperature(),
            breathRate = randomBreathRate(),
            rightConstriction = row[43],
            rightDiastolic = row[44],
            leftConstriction = row[41],
            leftDiastolic = row[42],
            height = row[80],
            weight = row[81],
            waistline = row[82],
            leftEye = randomEyeSight(),
            rightEye = randomEyeSight(),
            hemoglobin = row[59],
            whiteBloodCell = row[61],
            platelet = row[62],
            fastingBloodGlucose = row[51],
            isEcgNormal = row[78].trim() == "正常",
            serumAlanineAminotransferase = row[54],
            serumGlutamicOxalaceticTransaminase = row[55],
            totalBilirubin = row[57],
            serumCreatinine = row[72],
            bloodUreaNitrogen = row[73],
            totalCholesterol = row[68],
            triglyceride = row[69],
            serumLowDensityLipoproteinCholesterol = row[70],
            serumHighDensityLipoproteinCholesterol = row[71],
            chestXray = row[74].trim(),
            bulTrasonic = row[76].trim(),
            medicine1 = row[229],
            medicineUse1 = row[230],
            medicineEachDose1 = row[231],
            medicineUseDate1 = row[232],
            medicineYield1 = MedicineYield(row[233]),
            medicine2 = row[234],
            medicineUse2 = row[235],
            medicineEachDose2 = row[236],
            medicineUseDate2 = row[237],
            medicineYield2 = MedicineYield(row[238]),
            medicine3 = row[239],
            medicineUse3 = row[240],
            medicineEachDose3 = row[241],
            medicineUseDate3 = row[242],
            medicineYield3 = MedicineYield(row[243]),
            medicine4 = row[244],
            medicineUse4 = row[245],
            medicineEachDose4 = row[246],
            medicineUseDate4 = row[247],
            medicineYield4 = MedicineYield(row[248]),
            hasAbnormal = row[165].trim() == "有",
            abnormality1 = row[166],
            abnormality2 = row[167],
            abnormality3 = row[168],
            abnormality4 = row[169],
            isPutIntoAdministration = row[216].trim().isNotEmpty(),
            isSuggestedReview = row[217].trim().isNotEmpty(),
            isSuggestReferral = row[218].trim().isNotEmpty(),
            isNeedQuitSmoking = row[219].trim().isNotEmpty(),
            isNeedHealthDrinking = row[220].trim().isNotEmpty(),
            isNeedDiet = row[221].trim().isNotEmpty(),
            isNeedExercise = row[222].trim().isNotEmpty(),
            isNeedLostWeight = row[223].trim().isNotEmpty(),
            targetWeight = row[224],
            isSuggestVaccination = row[225].trim().isNotEmpty(),
            vaccine = row[226],
            hasOther = row[227].trim().isNotEmpty(),
            pjOther = row[228]
        )
    }
}

private fun WebDriver.selectOptionWhenAllNoSelected(nameValuePair: Pair<String, String>) {
    findElements { name(nameValuePair.first) }.none {
        it.isSelected
    }.let {
        if (it) {
            selectWhenNotSelect(nameValuePair)
        }
    }
}

private fun WebDriver.sendKeysWhenInputEmpty(nameValuePair: Pair<String, String>) {
    name(nameValuePair.first).sendKeysWhenInputEmpty(nameValuePair.second)
}

private fun WebDriver.sendKeysWhenValueNotEmpty(nameValuePair: Pair<String, String>) {
    name(nameValuePair.first).sendKeysWhenValueNotEmpty(nameValuePair.second)
}

private fun WebDriver.selectWhenNotSelect(nameValuePair: Pair<String, String>) {
    xpathByNameWithValue(nameValuePair.first, nameValuePair.second).selectWhenNotSelected()
}

/**
 * 36.6-37.2随机
 */
private fun randomTemperature(): String = (36.5 + Math.random() * 0.7).let { "%.1f".format(it) }

/**
 * 60-100随机
 */
private fun randomBreathRate(): String = (60 + Math.random() * 40).toInt().toString()

/**
 * 4.5-4.8随机
 */
private fun randomEyeSight(): String = (4.5 + Math.random() * 0.3).let { "%.1f".format(it) }