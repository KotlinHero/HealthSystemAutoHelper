package tech.kotlinhero.autohelper.core.business.task

import com.microsoft.playwright.Page
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import tech.kotlinhero.autohelper.core.ImportTaskParam
import tech.kotlinhero.autohelper.core.ProgressTask
import tech.kotlinhero.autohelper.core.TaskProgress
import tech.kotlinhero.autohelper.core.business.diabetes.gotoDiabetesRecordListPage
import tech.kotlinhero.autohelper.core.business.loginHealthSystem
import tech.kotlinhero.autohelper.core.excel.DiabetesRecord
import tech.kotlinhero.autohelper.core.excel.nextVisitDate
import tech.kotlinhero.autohelper.core.excel.toDiabetesRecord
import tech.kotlinhero.autohelper.core.emitLog
import tech.kotlinhero.autohelper.core.emitProgressUpdate
import tech.kotlinhero.autohelper.core.emitTotalCount
import tech.kotlinhero.autohelper.core.extension.flowOnDefault
import tech.kotlinhero.autohelper.excel.readExcel
import tech.kotlinhero.autohelper.webdriver.allByCss
import tech.kotlinhero.autohelper.webdriver.browserSession
import tech.kotlinhero.autohelper.webdriver.css
import tech.kotlinhero.autohelper.webdriver.name
import tech.kotlinhero.autohelper.webdriver.xpath
import kotlin.time.Duration.Companion.milliseconds

class DiabetesVisitImportTask(
    val params: ImportTaskParam,
) : ProgressTask {

    override val taskDescription: String = "导入糖尿病随访"

    override fun execute(): Flow<TaskProgress> = flowOnDefault {
        browserSession(params.browserExecutablePath).use { session ->
            with(session.page) {
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
    }

    private suspend fun Page.importDiabetesVisitRecord(visitRecord: DiabetesRecord) {
        name("idCard").fill(visitRecord.id)
        css("button.x-btn-text.query").click()
        css("table[class='x-grid3-row-table']").first().dblclick()
        xpath("//*[text() = '糖尿病随访']").click()
        allByCss("td.x-grid3-col.x-grid3-cell.x-grid3-td-0.x-grid3-cell-first")
            .firstOrNull { element ->
                element.locator("xpath=./div").innerText() == visitRecord.planDate
            }
            ?.click()
        css("[name*='visitDate']").fill(visitRecord.visitDate)
        css("input[type='radio'][id^='visitWay_'][value='${visitRecord.visitWay.toOption()}']").click()
        // 该下拉框疑似使用网络请求构建，增加延迟等待元素可点击
        css("div[id^='div_sfxz'] > div > img").click()
        delay(500.milliseconds)
        xpath("//div[text()='${visitRecord.visitNature.trim()}']").let {
            delay(500.milliseconds)
            // 未知原因会定位到两个相同的元素，一个元素只需要点击那个元素，两个元素时则需要点击第二个元素
            // 即需要点击最后一个元素
            it.last().click()
        }
        css("input[type='radio'][id^='visitEffect_'][value='1']").click()
        css(
            "input[type='checkbox'][id^='symptoms_'][value='${visitRecord.currentSymptom.toOption()}']"
        ).let {
            if (!it.isChecked) {
                it.click()
            }
        }
        name("constriction").fill(visitRecord.constriction)
        name("diastolic").fill(visitRecord.diastolic)
        css("input[id^='weight_']").fill(visitRecord.weight)
        css("input[id^='targetWeight_']").fill(visitRecord.targetWeight)
        css("input[type='checkbox'][id^='pulsation_'][value='1']").click()
        css("input[id^='fbs_']").fill(visitRecord.bloodGlucose)
        css("input[id^='smokeCount_']").fill(visitRecord.smokeCount)
        css("input[id^='targetSmokeCount_']").fill(visitRecord.targetSmokeCount)
        css("input[id^='drinkCount_']").fill(visitRecord.drinkCount)
        css("input[id^='targetDrinkCount_']").fill(visitRecord.targetDrinkCount)
        css("input[id^='trainTimesWeek_']").fill(visitRecord.trainTimesWeek)
        css("input[id^='trainMinute_']").fill(visitRecord.trainMinute)
        css("input[id^='targetTrainTimesWeek_']").fill(visitRecord.targetTrainTimesWeek)
        css("input[id^='targetTrainMinute_']").fill(visitRecord.targetTrainMinute)
        css("input[id^='otherSigns_']").fill(visitRecord.otherSigns)
        name("food").fill(visitRecord.food)
        name("targetFood").fill(visitRecord.targetFood)
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
            if (visitRecord.needDoubleVisit.value == "是") {
                it.fill(visitRecord.nextVisitDate)
            }
        }
        visitRecord.referralReason.takeIf { it.isNotEmpty() }?.let {
            css("div[id^='div_referralReason_'] > div > img").click()
            delay(300.milliseconds)
            // 与随访性质情况相同
            xpath("//*[text() = '连续两次出现空腹血糖控制不满意']").last().click()
            delay(300.milliseconds)
            css("div[id^='div_referralReason_'] > div > img").click()
        }
        visitRecord.agencyAndDept.takeIf { it.isNotEmpty() }?.let {
            delay(300.milliseconds)
            css("div[id^='div_agencyAndDept_'] > div > img").click()
            delay(300.milliseconds)
            // 与随访性质情况相同
            xpath("//*[text() = '${it.trim()}']").last().click()
        }
        xpath("//*[text() = '确定(F1)']").nth(1).click()
        delay(300.milliseconds)
        runCatching {
            xpath("//*[text() = '取消(F2)']").first().click()
        }
        delay(300.milliseconds)
        css("button[id='CLOSE']").click()
    }

    private suspend fun Page.prepareImport() {
        loginHealthSystem(params.username, params.password)
        gotoDiabetesRecordListPage()
    }
}
