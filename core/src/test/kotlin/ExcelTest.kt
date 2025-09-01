import tech.kotlinhero.autohelper.core.excel.toHypertensionVisitRecord
import tech.kotlinhero.autohelper.excel.readExcel
import kotlin.test.Test
import kotlin.test.assertEquals

class ExcelTest {
    @Test
    fun test() {
        readExcel("C:\\Users\\slowp\\Desktop\\冲边村.xlsx") {
            val sheet = getSheetAt(1)
            val headCount = 1
            val totalCount = sheet.lastRowNum
            assertEquals(312, totalCount)
            sheet.drop(headCount).forEachIndexed { index, row ->
                val visitRecord = row.toHypertensionVisitRecord()
                println("第${index + 1}位: $visitRecord")
            }
        }
    }
}