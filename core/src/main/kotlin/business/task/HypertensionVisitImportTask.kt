package tech.kotlinhero.autohelper.core.business.task

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.core.*
import tech.kotlinhero.autohelper.core.business.gotoHypertensionRecordListPage
import tech.kotlinhero.autohelper.core.business.loginHealthSystem
import tech.kotlinhero.autohelper.core.excel.HypertensionRecord
import tech.kotlinhero.autohelper.core.excel.nextVisitDate
import tech.kotlinhero.autohelper.core.excel.toHypertensionVisitRecord
import tech.kotlinhero.autohelper.core.extension.flowOnDefault
import tech.kotlinhero.autohelper.excel.readExcel
import tech.kotlinhero.autohelper.webdriver.*
import kotlin.time.Duration.Companion.milliseconds

class HypertensionVisitImportTask(
    private val params: ImportTaskParam
) : ProgressTask {

    override val taskDescription: String = "导入高血压随访"

    override fun execute(): Flow<TaskProgress> = flowOnDefault {
        params.buildWebDriver().use {
            readExcel(params.excelPath) {
                val sheet = getSheetAt(1)
                val headRowCount = 1
                emitTotalCount(sheet.lastRowNum)
                emitLog("正在登录系统准备导入")
                prepareImport()
                sheet.drop(headRowCount).forEachIndexed { rowIndex, row ->
                    val visitRecord = row.toHypertensionVisitRecord()
                    val currentDataIdentifier = "${visitRecord.name}-${visitRecord.id}"
                    emitLog("开始导入：$currentDataIdentifier")
                    runCatching {
                        importHyperVisitRecord(visitRecord)
                    }.fold(
                        onSuccess = {
                            emitLog("导入成功：$currentDataIdentifier")
                            emitProgressUpdate(rowIndex + 1)
                        },
                        onFailure = {
                            emitLog("导入失败：$currentDataIdentifier")
                            it.printStackTrace()
                            emitProgressUpdate(rowIndex + 1)
                            prepareImport()
                        }
                    )
                }
            }
        }
    }

    private suspend fun WebDriver.importHyperVisitRecord(visitRecord: HypertensionRecord) {
        name("idCard").clearSendKeys(visitRecord.id)
        css("button.x-btn-text.query").click()
        doubleClick {
            css("table[class='x-grid3-row-table']")
        }
        xpath("//*[text() = '高血压随访']").click()
        allByCss("td.x-grid3-col.x-grid3-cell.x-grid3-td-0.x-grid3-cell-first").find { element ->
            element.findElement { xpath("./div") }.text == visitRecord.planDate
        }?.click()
        runCatching {
            xpath("//*[text() = '确定']").click()
        }
        css("[name*='visitDate']").clearSendKeys(visitRecord.visitDate)
        css("input[type='radio'][name^='visitWay_'][value='${visitRecord.visitWay.toOption()}']").click()
        //该下拉框疑似使用网络请求构建，增加延迟等待元素可点击
        css("div[id^='div_sfxz'] > div > img").click()
        delay(500)
        findElements {
            xpath("//div[text()='${visitRecord.visitNature.trim()}']")
        }.let {
            delay(500)
            //未知原因会定位到两个相同的元素，一个元素只需要点击那个元素，两个元素时则需要点击第二个元素
            //即需要点击最后一个元素
            it.last().click()
        }
        css("input[type='radio'][name^='visitEffect_'][value='1']").click()
        css(
            "input[type='checkbox'][name^='currentSymptoms_'][value='${visitRecord.currentSymptom.toOption()}']"
        ).let {
            if (!it.isSelected) {
                it.click()
            }
        }
        css("input[id^='constriction_']").clearSendKeys(visitRecord.constriction)
        css("input[id^='diastolic_']").clearSendKeys(visitRecord.diastolic)
        css("input[id^='weight_']").clearSendKeys(visitRecord.weight)
        css("input[id^='targetWeight_']") {
            clear()
            clearSendKeys(visitRecord.targetWeight)
        }
        css("input[id^='heartRate_']").clearSendKeys(visitRecord.heartRate)
        css("input[id^='otherSigns_']").clearSendKeys("无")
        css("input[id^='auxiliaryCheck_']").clearSendKeys("无")
        css("input[id^='smokeCount_']").clearSendKeys(visitRecord.smokeCount)
        css("input[id^='targetSmokeCount_']").clearSendKeys(visitRecord.targetSmokeCount)
        css("input[id^='drinkCount_']").clearSendKeys(visitRecord.drinkCount)
        css("input[id^='targetDrinkCount_']").clearSendKeys(visitRecord.targetDrinkCount)
        css("input[id^='trainTimesWeek_']").clearSendKeys(visitRecord.trainTimesWeek)
        css("input[id^='trainMinute_']").clearSendKeys(visitRecord.trainMinute)
        css("input[id^='targetTrainTimesWeek_']").clearSendKeys(visitRecord.targetTrainTimesWeek)
        css("input[id^='targetTrainMinute_']").clearSendKeys(visitRecord.targetTrainMinute)
        css("input[type='radio'][name^='salt_'][value='${visitRecord.salt.toOption()}']").click()
        css("input[type='radio'][name^='targetSalt_'][value='${visitRecord.targetSalt.toOption()}']").click()
        css(
            "input[type='radio'][name^='psychologyChange_'][value='${visitRecord.psychologyChange.toOption()}']"
        ).click()
        css("input[type='radio'][name^='obeyDoctor_'][value='${visitRecord.obeyDoctor.toOption()}']").click()
        css("input[type='radio'][name^='medicine_'][value='${visitRecord.medicine.toOption()}']").click()
        runCatching {
            css("input[type='radio'][name^='medicineBadEffect_'][value='n']").click()
        }
        css("input[type='radio'][name^='visitEvaluate_'][value='${visitRecord.visitEvaluate.toOption()}']").click()
        css("input[type='radio'][name^='needdoublevisit_'][value='${visitRecord.needDoubleVisit.toOption()}']").click()
        css("input[type='text'][name^='nextDate_']").let {
            if (visitRecord.needDoubleVisit.value == "是") {
                it.clearSendKeys(visitRecord.nextVisitDate)
            }
        }
        visitRecord.referralReason.takeIf { it.isNotEmpty() }?.let {
            delay(300)
            css("div[id^='div_referralReason_'] > div > img").click()
            delay(300)
            findElements {
                xpath("//*[text() = '连续两次出现血压控制不满意']")
            }.last().click()
        }
        visitRecord.agencyAndDept.takeIf { it.isNotEmpty() }?.let {
            delay(300.milliseconds)
            css("div[id^='div_agencyAndDept_'] > div > img").click()
            delay(300.milliseconds)
            findElements {
                xpath("//*[text() = '${it.trim()}']")
            }.last().click()
        }
        findElements {
            xpath("//*[text() = '确定(F1)']")
        }[1].click()
        delay(2500.milliseconds)
        runCatching {
            xpath("//*[text() = '确定']").click()
        }
        css("button[id='CLOSE']").click()
    }

    private suspend fun WebDriver.prepareImport() {
        loginHealthSystem(params.username, params.password)
        gotoHypertensionRecordListPage()
    }
}