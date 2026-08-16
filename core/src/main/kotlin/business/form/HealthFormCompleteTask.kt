package tech.kotlinhero.autohelper.core.business.form

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import org.apache.poi.ss.usermodel.Row
import com.microsoft.playwright.Page
import tech.kotlinhero.autohelper.core.*
import tech.kotlinhero.autohelper.core.business.loginHealthSystem
import tech.kotlinhero.autohelper.core.excel.ExcelRowMapper
import tech.kotlinhero.autohelper.core.task.HealthExcelListProgressTask
import tech.kotlinhero.autohelper.core.task.HealthRecordDescription
import tech.kotlinhero.autohelper.core.task.HealthRecordImporter
import tech.kotlinhero.autohelper.core.task.SimpleRecordDescription
import tech.kotlinhero.autohelper.excel.get
import tech.kotlinhero.autohelper.webdriver.browserSession
import tech.kotlinhero.autohelper.webdriver.css
import tech.kotlinhero.autohelper.webdriver.firstByXpath
import tech.kotlinhero.autohelper.webdriver.name
import tech.kotlinhero.autohelper.webdriver.xpath
import kotlin.time.Duration.Companion.milliseconds

class HealthFormCompleteTask(
    private val browserExecutablePath: String,
    private val healthSystemAuthentication: HealthSystemAuthentication,
    private val excelFilePath: String
) : ProgressTask {

    override val taskDescription: String = "完善体检表任务"

    override fun execute(): Flow<TaskProgress> {
        val session = browserSession(browserExecutablePath)
        return HealthExcelListProgressTask(
            page = session.page,
            importer = HealthFormCompleteImporter(
                page = session.page,
                authentication = healthSystemAuthentication
            ),
            excelRowMapper = HealthFormExcelRowMapper(),
            excelFilePath = excelFilePath
        ).execute().onCompletion {
            session.close()
        }
    }
}

private class HealthFormCompleteImporter(
    private val page: Page,
    private val authentication: HealthSystemAuthentication
) : HealthRecordImporter<HealthFormCompleteRecord> {

    override suspend fun prepareImport() = page.run {
        loginHealthSystem(authentication)
        gotoHealthFormListPage()
    }

    override suspend fun importRecord(record: HealthFormCompleteRecord): Unit = page.run {
        name("idCard").fill(record.id)
        css("button.x-btn-text.query").click()
        css("table[class='x-grid3-row-table']").first().dblclick()
        delay(1000.milliseconds)
        xpath("//div[@class='x-grid3-cell-inner x-grid3-col-0']").all()
            .firstOrNull { it.innerText() == record.checkDate }
            ?.click() ?: throw IllegalArgumentException()

        delay(1500.milliseconds)
        record.checkWays.forEach { checkWay ->
            selectWhenNotChecked("checkWay" to checkWay)
        }
        selectWhenNotChecked("symptom" to "01")
        listOf(
            "temperature" to record.temperature,
            "breathe" to record.breathRate
        ).forEach { fillWhenInputEmpty(it) }

        listOf(
            "pulse" to record.heartRate,
            "constriction" to record.rightConstriction,
            "diastolic" to record.rightDiastolic,
            "constriction_L" to record.leftConstriction,
            "diastolic_L" to record.leftDiastolic,
            "height" to record.height,
            "weight" to record.weight,
            "waistline" to record.waistline
        ).forEach { fillWhenValueNotEmpty(it) }

        if (record.isElder) {
            listOf(
                "healthStatus" to "1",
                "selfCare" to "1"
            ).forEach { selectWhenNotChecked(it) }
        }

        listOf(
            "cognitive" to "1",
            "emotion" to "1",
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
        ).forEach { fillWhenInputEmpty(it) }

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

        fillWhenValueNotEmpty("heartRate" to record.heartRate)
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
            selectWhenNotChecked("footPulse" to "2")
        }

        listOf(
            "hgb" to record.hemoglobin,
            "wbc" to record.whiteBloodCell,
            "platelet" to record.platelet
        ).forEach { fillWhenValueNotEmpty(it) }

        fillWhenInputEmpty("fbs" to record.fastingBloodGlucose)

        selectWhenNotChecked("ecg" to if (record.isEcgNormal) "1" else "2")

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
        ).forEach { fillWhenValueNotEmpty(it) }

        if (record.chestXray.isNotEmpty()) {
            selectWhenNotChecked("x" to if (record.chestXray == "正常") "1" else "2")
        }
        if (record.bulTrasonic.isNotEmpty()) {
            selectWhenNotChecked("b" to if (record.bulTrasonic == "正常") "1" else "2")
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
            selectWhenNotChecked("otherDiseasesone" to "2")
            fillWhenValueNotEmpty("otherDiseasesoneDesc" to otherDiseases)
        } else {
            selectWhenNotChecked("otherDiseasesone" to "1")
        }

        delay(500.milliseconds)

        listOf(
            "inhospitalFlag" to "n",
            "infamilybedFlag" to "n",
            "medicineFlag" to record.medicineFlag
        ).forEach { selectWhenNotChecked(it) }

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
        ).forEach { fillWhenValueNotEmpty(it) }

        listOf(
            "medicineYield1" to record.medicineYield1,
            "medicineYield2" to record.medicineYield2,
            "medicineYield3" to record.medicineYield3,
            "medicineYield4" to record.medicineYield4,
        ).forEach { pair ->
            pair.second.option()?.let { option ->
                selectWhenNotChecked(pair.first to option)
            }
        }

        selectWhenNotChecked("nonimmuneFlag" to "n")

        if (record.hasAbnormal) {
            selectWhenNotChecked("abnormality" to "2")
            listOf(
                "abnormality1" to record.abnormality1,
                "abnormality2" to record.abnormality2,
                "abnormality3" to record.abnormality3,
                "abnormality4" to record.abnormality4
            ).forEach {
                fillWhenValueNotEmpty(it)
            }
        } else {
            selectWhenNotChecked("abnormality" to "1")
        }

        record.manas.forEach {
            selectWhenNotChecked("mana" to it)
        }

        record.riskControls.forEach {
            selectWhenNotChecked("riskfactorsControl" to it)
        }
        if (record.isNeedLostWeight) {
            fillWhenValueNotEmpty("targetWeight" to record.targetWeight)
        }
        if (record.isSuggestVaccination) {
            fillWhenValueNotEmpty("vaccine" to record.vaccine)
        }
        if (record.hasOther) {
            fillWhenValueNotEmpty("pjOther" to record.pjOther)
        }

        firstByXpath("//*[text() = '确定(F1)']")?.click()
        delay(2000.milliseconds)
        css("button[id='CLOSE']").click()
    }
}

private data class HealthFormCompleteRecord(
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
    val hemoglobin: String,
    val whiteBloodCell: String,
    val platelet: String,
    override val fastingBloodGlucose: String,
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
    val abnormality2: String,
    val abnormality3: String,
    val abnormality4: String,
    override val isPutIntoAdministration: Boolean,
    override val isSuggestedReview: Boolean,
    override val isSuggestReferral: Boolean,
    override val isNeedQuitSmoking: Boolean,
    override val isNeedHealthDrinking: Boolean,
    override val isNeedDiet: Boolean,
    override val isNeedExercise: Boolean,
    override val isNeedLostWeight: Boolean,
    override val targetWeight: String,
    val isSuggestVaccination: Boolean,
    val vaccine: String,
    override val hasOther: Boolean,
    override val pjOther: String,
) : HealthForm, HealthRecordDescription by SimpleRecordDescription(id, name)


private val HealthFormCompleteRecord.medicineFlag: String
    get() = if (medicineUse1.isEmpty()) "n" else "y"

private val HealthFormCompleteRecord.riskControls: List<String>
    get() = listOf(
        isNeedQuitSmoking to "1",
        isNeedHealthDrinking to "2",
        isNeedDiet to "3",
        isNeedExercise to "4",
        isNeedLostWeight to "5",
        isSuggestVaccination to "6",
        hasOther to "7"
    ).asSequence().filter { it.first }.map { it.second }.toList()

private class HealthFormExcelRowMapper : ExcelRowMapper<HealthFormCompleteRecord> {
    override val dropCount: Int = 3
    override val sheetIndex: Int = 0

    override fun mapRowTo(row: Row): HealthFormCompleteRecord {
        return HealthFormCompleteRecord(
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