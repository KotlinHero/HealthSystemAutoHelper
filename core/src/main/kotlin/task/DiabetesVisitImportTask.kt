package tech.kotlinhero.autohelper.core.task

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.core.*
import tech.kotlinhero.autohelper.core.excel.DiabetesVisitRecord
import tech.kotlinhero.autohelper.core.excel.toDiabetesVisitRecord
import tech.kotlinhero.autohelper.excel.readExcel
import tech.kotlinhero.autohelper.webdriver.*

class DiabetesVisitImportTask(
    val params: ImportTaskParam
) : ProgressLogTask {
    override val taskDescription: String = "导入糖尿病随访"

    override fun execute(): Flow<TaskProgress> = flow {
        val driver = params.buildWebDriver()
        readExcel(params.excelPath) {
            val sheet = getSheetAt(2)
            val headRowCount = 1
            totalCount(sheet.lastRowNum)

            driver.run {
                log("正在登录系统准备导入")
                prepareImport()
                sheet.drop(headRowCount).forEachIndexed { rowIndex, row ->
                    val visitRecord = row.toDiabetesVisitRecord()
                    val currentDataIdentifier = "${visitRecord.name}-${visitRecord.id}"
                    log("开始导入：$currentDataIdentifier")
                    runCatching {
                        importDiabetesVisitRecord(visitRecord)
                    }.fold(
                        onSuccess = {
                            log("导入成功：$currentDataIdentifier")
                            progressUpdate(rowIndex + 1)
                        },
                        onFailure = {
                            log("导入失败：$currentDataIdentifier")
                            progressUpdate(rowIndex + 1)
                            prepareImport()
                        }
                    )
                }
            }
        }
    }.flowOn(Dispatchers.Default)

    private fun WebDriver.importDiabetesVisitRecord(visitRecord: DiabetesVisitRecord) {
        name("idCard").clearSendKeys(visitRecord.id)
        css("button.x-btn-text.query").click()
        doubleClick {
            css("table[class='x-grid3-row-table']")
        }
        xpath("//*[text() = '糖尿病随访']").click()
        allByCss("td.x-grid3-col.x-grid3-cell.x-grid3-td-0.x-grid3-cell-first").find { element ->
            element.findElement { xpath("./div") }.text == visitRecord.planDate
        }?.click()
        css("[name*='visitDate']").clearSendKeys(visitRecord.visitDate)
        css("input[type='radio'][id^='visitWay_'][value='${visitRecord.visitWay.toOption()}']").click()
        css("div[id^='div_sfxz'] > div > img").click()
        xpath("//*[text() = '${visitRecord.visitNature}']").click()
        css("input[type='radio'][id^='visitEffect_'][value='1']").click()
        css(
            "input[type='checkbox'][id^='symptoms_'][value='${visitRecord.currentSymptom.toOption()}']"
        ).click()
        name("constriction").clearSendKeys(visitRecord.constriction)
        name("diastolic").clearSendKeys(visitRecord.diastolic)
        css("input[id^='weight_']").clearSendKeys(visitRecord.weight)
        css("input[id^='targetWeight_']") {
            clear()
            clearSendKeys(visitRecord.targetWeight)
        }
        css("input[id^='fbs_']").clearSendKeys(visitRecord.bloodGlucose)
        css("input[id^='smokeCount_']").clearSendKeys(visitRecord.smokeCount)
        css("input[id^='targetSmokeCount_']").clearSendKeys(visitRecord.targetSmokeCount)
        css("input[id^='drinkCount_']").clearSendKeys(visitRecord.drinkCount)
        css("input[id^='targetDrinkCount_']").clearSendKeys(visitRecord.targetDrinkCount)
        css("input[id^='trainTimesWeek_']").clearSendKeys(visitRecord.trainTimesWeek)
        css("input[id^='trainMinute_']").clearSendKeys(visitRecord.trainMinute)
        css("input[id^='targetTrainTimesWeek_']").clearSendKeys(visitRecord.targetTrainTimesWeek)
        css("input[id^='targetTrainMinute_']").clearSendKeys(visitRecord.targetTrainMinute)
        css("input[id^='otherSigns_']").clearSendKeys(visitRecord.otherSigns)
        name("food").clearSendKeys(visitRecord.food)
        name("targetFood").clearSendKeys(visitRecord.targetFood)
        css("input[id^='medicine_'][value='${visitRecord.medicine.toOption()}']").click()
        css("input[type='radio'][id^='visitType_'][value='${visitRecord.visitType.toOption()}']").click()
        css("input[type='radio'][id^='needdoublevisit_'][value='${visitRecord.needDoubleVisit.toOption()}']").click()
        visitRecord.referralReason.takeIf { it.isNotEmpty() }?.let {
            css("div[id^='div_referralReason_'] > div > img").click()
            xpath("//*[text() = '连续两次出现空腹血糖控制不满意']").click()
        }
        visitRecord.agencyAndDept.takeIf { it.isNotEmpty() }?.let {
            css("div[id^='div_agencyAndDept_'] > div > img").click()
            xpath("//*[text() = '界牌镇中心卫生院慢病门诊']").click()
        }
        findElements {
            xpath("//*[text() = '确定(F1)']")
        }[1].click()
        xpath("//*[text() = '取消(F2)']").click()
        css("button[id='CLOSE']").click()
    }

    private fun WebDriver.prepareImport() {
        healthSystemImportTask { loginHealthSystem(params.username, params.password) }
        css(
            "html > body > div:nth-of-type(1) > div > div > div:nth-of-type(1) > ul > li:nth-of-type(2) > a"
        ).click()
        xpath("//a[text()='糖尿病管理']").click()
        css("a[title='糖尿病档案管理']").click()
        allByCss("img.x-form-trigger.x-form-arrow-trigger").getOrNull(0)?.click()
        css("html > body > div:nth-of-type(8) > div > div:nth-of-type(6)").click()
    }
}