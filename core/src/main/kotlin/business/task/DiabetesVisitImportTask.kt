package tech.kotlinhero.autohelper.core.business.task

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import org.openqa.selenium.WebDriver
import tech.kotlinhero.autohelper.core.*
import tech.kotlinhero.autohelper.core.business.diabetes.gotoDiabetesRecordListPage
import tech.kotlinhero.autohelper.core.business.loginHealthSystem
import tech.kotlinhero.autohelper.core.excel.DiabetesRecord
import tech.kotlinhero.autohelper.core.excel.nextVisitDate
import tech.kotlinhero.autohelper.core.excel.toDiabetesRecord
import tech.kotlinhero.autohelper.core.extension.flowOnDefault
import tech.kotlinhero.autohelper.excel.readExcel
import tech.kotlinhero.autohelper.webdriver.*

class DiabetesVisitImportTask(
    val params: ImportTaskParam
) : ProgressTask {

    override val taskDescription: String = "导入糖尿病随访"

    override fun execute(): Flow<TaskProgress> = flowOnDefault {
        params.buildWebDriver().use {
            readExcel(params.excelPath) {
                val sheet = getSheetAt(2)
                val headRowCount = 1
                emitTotalCount(sheet.lastRowNum)
                emitLog("正在登录系统准备导入")
                prepareImport()
                sheet.drop(headRowCount).forEachIndexed { rowIndex, row ->
                    val visitRecord = row.toDiabetesRecord()
                    val currentDataIdentifier = "${visitRecord.name}-${visitRecord.id}"
                    emitLog("开始导入：$currentDataIdentifier")
                    runCatching {
                        importDiabetesVisitRecord(visitRecord)
                    }.fold(
                        onSuccess = {
                            emitLog("导入成功：$currentDataIdentifier")
                            emitProgressUpdate(rowIndex + 1)
                        },
                        onFailure = {
                            emitLog("导入失败：$currentDataIdentifier")
                            emitProgressUpdate(rowIndex + 1)
                            prepareImport()
                        }
                    )
                }
            }
        }
    }

    private suspend fun WebDriver.importDiabetesVisitRecord(visitRecord: DiabetesRecord) {
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
        css("input[type='radio'][id^='visitEffect_'][value='1']").click()
        css(
            "input[type='checkbox'][id^='symptoms_'][value='${visitRecord.currentSymptom.toOption()}']"
        ).let {
            if (!it.isSelected) {
                it.click()
            }
        }
        name("constriction").clearSendKeys(visitRecord.constriction)
        name("diastolic").clearSendKeys(visitRecord.diastolic)
        css("input[id^='weight_']").clearSendKeys(visitRecord.weight)
        css("input[id^='targetWeight_']") {
            clear()
            clearSendKeys(visitRecord.targetWeight)
        }
        css("input[type='checkbox'][id^='pulsation_'][value='1']").click()
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
        css("input[id^='psychologyChange_'][value='${visitRecord.psychologyChange.toOption()}']").click()
        css("input[id^='obeyDoctor_'][value='${visitRecord.obeyDoctor.toOption()}']").click()
        css("input[id^='medicine_'][value='${visitRecord.medicine.toOption()}']").click()
        runCatching {
            css("input[type='radio'][name^='medicineBadEffect_'][value='n']").click()
        }
        css("input[type='radio'][id^='visitType_'][value='${visitRecord.visitType.toOption()}']").click()
        css("input[type='radio'][id^='needdoublevisit_'][value='${visitRecord.needDoubleVisit.toOption()}']").click()
        css("input[type='radio'][id^='adverseReactions_'][value='1']").click()
        css("input[type='radio'][id^='glycopenia_'][value='1']").click()
        css("input[type='text'][name^='nextDate']").let {
            if (visitRecord.needDoubleVisit.value == "是"
            ) {
                it.clearSendKeys(visitRecord.nextVisitDate)
            }
        }
        visitRecord.referralReason.takeIf { it.isNotEmpty() }?.let {
            css("div[id^='div_referralReason_'] > div > img").click()
            delay(300)
            //与随访性质情况相同
            findElements {
                xpath("//*[text() = '连续两次出现空腹血糖控制不满意']")
            }.last().click()
            delay(300)
            css("div[id^='div_referralReason_'] > div > img").click()
        }
        visitRecord.agencyAndDept.takeIf { it.isNotEmpty() }?.let {
            delay(300)
            css("div[id^='div_agencyAndDept_'] > div > img").click()
            delay(300)
            //与随访性质情况相同
            findElements {
                xpath("//*[text() = '界牌镇中心卫生院慢病门诊']")
            }.last().click()
        }
        findElements {
            xpath("//*[text() = '确定(F1)']")
        }[1].click()
        delay(300)
        runCatching {
            xpath("//*[text() = '取消(F2)']").click()
        }
        delay(300)
        css("button[id='CLOSE']").click()
    }

    private fun WebDriver.prepareImport() {
        loginHealthSystem(params.username, params.password)
        gotoDiabetesRecordListPage()
    }
}