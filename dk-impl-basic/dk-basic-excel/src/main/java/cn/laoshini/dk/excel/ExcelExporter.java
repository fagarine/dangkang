package cn.laoshini.dk.excel;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

import cn.laoshini.dk.domain.common.ConstTable;

/**
 * excel数据导出
 *
 * @author fagarine
 */
class ExcelExporter extends AbstractExcelExporter {

    private List<ConstTable<?>> tables;

    /**
     * 多表单数据导出
     *
     * @param tables 表单数据
     * @param output 输出流
     */
    public ExcelExporter(List<ConstTable<?>> tables, OutputStream output) {
        this.tables = tables;
        this.output = output;
    }

    /**
     * 单个表单数据导出
     *
     * @param table 表单数据
     * @param output 输出流
     */
    public ExcelExporter(ConstTable<?> table, OutputStream output) {
        this.tables = new ArrayList<>(1);
        this.tables.add(table);
        this.output = output;
    }

    @Override
    protected Workbook writeToWorkbook() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        if (tables != null) {
            for (ConstTable<?> table : tables) {
                HSSFSheet sheet = initSheetAndHeader(workbook, table.getTableName(), table.getHeaders());
                if (table.isEmpty()) {
                    continue;
                }

                // 将数据放入sheet中
                fillRows(sheet, table.getRows());
            }
        }
        return workbook;
    }

    /**
     * 初始化excel表单和表头
     *
     * @param workbook 工作簿对象
     * @param sheetTitle 表单名称
     * @param headers 表头内容
     * @return 返回表单对象
     */
    private HSSFSheet initSheetAndHeader(HSSFWorkbook workbook, String sheetTitle, List<String> headers) {
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet(sheetTitle);
        // 设置表格默认列宽15个字节
        sheet.setDefaultColumnWidth(15);
        // 生成一个样式
        CellStyle style = createCellStyle(workbook);
        // 生成一个字体
        Font font = createFont(workbook);
        // 把字体应用到当前样式
        style.setFont(font);

        // 生成并填写表头内容
        fillSheetHeaders(sheet, style, headers);
        return sheet;
    }

    /**
     * 生成并填写表头内容
     *
     * @param sheet excel表单对象
     * @param style 表单样式
     * @param headers 表头内容
     */
    private static void fillSheetHeaders(HSSFSheet sheet, CellStyle style, List<String> headers) {
        //生成表格标题
        HSSFRow header = sheet.createRow(0);
        header.setHeight((short) 300);

        if (headers == null || headers.isEmpty()) {
            return;
        }

        HSSFCell cell;
        for (int i = 0; i < headers.size(); i++) {
            cell = header.createCell(i);
            cell.setCellStyle(style);
            HSSFRichTextString text = new HSSFRichTextString(headers.get(i));
            cell.setCellValue(text);
        }
    }

    /**
     * 生成并返回单元格格式
     *
     * @param workbook 工作簿对象
     * @return CellStyle
     */
    private CellStyle createCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setLeftBorderColor(BorderStyle.THIN.getCode());
        style.setRightBorderColor(BorderStyle.THIN.getCode());
        style.setAlignment(HorizontalAlignment.CENTER);

        return style;
    }

    /**
     * 生成并返回字体样式
     *
     * @param workbook 工作簿对象
     * @return HSSFFont
     */
    private Font createFont(Workbook workbook) {
        Font font = workbook.createFont();
        font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        return font;
    }
}
