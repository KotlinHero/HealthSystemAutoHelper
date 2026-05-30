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
import tech.kotlinhero.autohelper.webdriver.clearSendKeys
import tech.kotlinhero.autohelper.webdriver.css
import tech.kotlinhero.autohelper.webdriver.doubleClick
import tech.kotlinhero.autohelper.webdriver.firstByXpath
import tech.kotlinhero.autohelper.webdriver.name
import kotlin.time.Duration.Companion.milliseconds

class HealthFormCreateTask(
    private val browserDriverConfig: BrowserDriverConfig,
    private val healthSystemAuthentication: HealthSystemAuthentication,
    private val excelFilePath: String
) : ProgressTask {

    override val taskDescription: String = "新建体检表任务"

    override fun execute(): Flow<TaskProgress> {
        val driver = browserDriverConfig.buildWebDriver()
        return HealthExcelListProgressTask(
            driver = driver,
            importer = HealthFormCreateImporter(
                driver = driver,
                authentication = healthSystemAuthentication
            ),
            excelRowMapper = HealthFormCreateRowMapper(),
            excelFilePath = excelFilePath
        ).execute().onCompletion {
            driver.quit()
        }
    }
}

private class HealthFormCreateImporter(
    private val driver: WebDriver,
    private val authentication: HealthSystemAuthentication
) : HealthRecordImporter<HealthFormCreateRecord> {

    override suspend fun prepareImport() = driver.run {
        loginHealthSystem(authentication)
        gotoHealthFormListPage()
    }

    override suspend fun importRecord(record: HealthFormCreateRecord): Unit = driver.run {
        name("idCard").clearSendKeys(record.id)
        css("button.x-btn-text.query").click()
        doubleClick {
            css("table[class='x-grid3-row-table']")
        }
        delay(5000.milliseconds)
        firstByXpath("//*[text() = '新建(F2)']")?.click()
        delay(2000.milliseconds)
        name("checkDate").clearSendKeys(record.checkDate)

        delay(1500.milliseconds)
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
                "selfCare" to "1",
                "cognitive" to "1",
                "emotion" to "1",
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

        sendKeysWhenInputEmpty("fbs" to record.fastingBloodGlucose)

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

        listOf(
            "inhospitalFlag" to "n",
            "infamilybedFlag" to "n",
            "medicineFlag" to "y"
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

        record.run {
            if (isElder && isDiabetes && !isHypertension) {
                selectWhenNotSelect("riskfactorsControl" to "6")
                sendKeysWhenValueNotEmpty("vaccine" to "流感，肺炎疫苗")
            }
        }

        if (record.hasOther) {
            sendKeysWhenValueNotEmpty("pjOther" to record.pjOther)
        }

        firstByXpath("//*[text() = '确定(F1)']")?.click()
        delay(2000.milliseconds)
        css("button[id='CLOSE']").click()
    }
}

private data class HealthFormCreateRecord(
    override val checkDate: String,
    override val name: String,
    override val id: String,
    override val isElder: Boolean,
    override val isHypertension: Boolean,
    override val isDiabetes: Boolean,
    override val heartRate: String,
    override val temperature: String,
    override val breathRate: String,
    override val rightConstriction: String,
    override val rightDiastolic: String,
    override val leftConstriction: String,
    override val leftDiastolic: String,
    override val height: String,
    override val weight: String,
    override val waistline: String,
    override val leftEye: String,
    override val rightEye: String,
    override val fastingBloodGlucose: String,
    override val medicineUse1: String,
    override val medicine1: String,
    override val medicineUseDate1: String,
    override val medicineEachDose1: String,
    override val medicineYield1: MedicineYield,
    override val medicineUse2: String,
    override val medicine2: String,
    override val medicineUseDate2: String,
    override val medicineEachDose2: String,
    override val medicineYield2: MedicineYield,
    override val medicineUse3: String,
    override val medicine3: String,
    override val medicineUseDate3: String,
    override val medicineEachDose3: String,
    override val medicineYield3: MedicineYield,
    override val medicineUse4: String,
    override val medicine4: String,
    override val medicineUseDate4: String,
    override val medicineEachDose4: String,
    override val medicineYield4: MedicineYield,
    override val hasAbnormal: Boolean,
    override val abnormality1: String,
    override val isPutIntoAdministration: Boolean,
    override val isSuggestedReview: Boolean,
    override val isSuggestReferral: Boolean,
    override val isNeedQuitSmoking: Boolean,
    override val isNeedHealthDrinking: Boolean,
    override val isNeedDiet: Boolean,
    override val isNeedExercise: Boolean,
    override val isNeedLostWeight: Boolean,
    override val targetWeight: String,
    override val hasOther: Boolean,
    override val pjOther: String,
) : HealthForm, HealthRecordDescription by SimpleRecordDescription(id, name)

private val HealthFormCreateRecord.riskControls: List<String>
    get() = listOf(
        isNeedQuitSmoking to "1",
        isNeedHealthDrinking to "2",
        isNeedDiet to "3",
        isNeedExercise to "4",
        isNeedLostWeight to "5",
        hasOther to "7"
    ).asSequence().filter { it.first }.map { it.second }.toList()

private class HealthFormCreateRowMapper : ExcelRowMapper<HealthFormCreateRecord> {

    override val dropCount: Int = 2

    override val sheetIndex: Int = 0

    override fun mapRowTo(row: Row): HealthFormCreateRecord {
        return HealthFormCreateRecord(
            checkDate = row[1],
            name = row[2],
            id = row[6],
            isElder = row[7].isNotEmpty(),
            isHypertension = row[8].isNotEmpty(),
            isDiabetes = row[9].isNotEmpty(),

            heartRate = row[10],
            temperature = randomTemperature(),
            breathRate = randomBreathRate(),
            rightConstriction = row[13],
            rightDiastolic = row[14],
            leftConstriction = row[11],
            leftDiastolic = row[12],
            height = row[16],
            weight = row[17],
            waistline = row[18],

            leftEye = randomEyeSight(),
            rightEye = randomEyeSight(),

            fastingBloodGlucose = row[15],

            medicine1 = row[56],
            medicineUse1 = row[57],
            medicineEachDose1 = row[58],
            medicineUseDate1 = row[59],
            medicineYield1 = MedicineYield(row[60]),
            medicine2 = row[61],
            medicineUse2 = row[62],
            medicineEachDose2 = row[63],
            medicineUseDate2 = row[64],
            medicineYield2 = MedicineYield(row[65]),
            medicine3 = row[66],
            medicineUse3 = row[67],
            medicineEachDose3 = row[68],
            medicineUseDate3 = row[69],
            medicineYield3 = MedicineYield(row[70]),
            medicine4 = row[71],
            medicineUse4 = row[72],
            medicineEachDose4 = row[73],
            medicineUseDate4 = row[74],
            medicineYield4 = MedicineYield(row[75]),
            hasAbnormal = row[29].isNotEmpty(),
            abnormality1 = row[29],

            isPutIntoAdministration = row[45].trim().isNotEmpty(),
            isSuggestedReview = row[46].trim().isNotEmpty(),
            isSuggestReferral = row[47].trim().isNotEmpty(),
            isNeedQuitSmoking = row[48].trim().isNotEmpty(),
            isNeedHealthDrinking = row[49].trim().isNotEmpty(),
            isNeedDiet = row[50].trim().isNotEmpty(),
            isNeedExercise = row[51].trim().isNotEmpty(),
            isNeedLostWeight = row[52].trim().isNotEmpty(),
            targetWeight = row[53],
            hasOther = row[54].trim().isNotEmpty(),
            pjOther = row[55]
        )
    }
}