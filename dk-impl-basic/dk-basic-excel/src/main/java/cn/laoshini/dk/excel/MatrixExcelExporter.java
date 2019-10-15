package cn.laoshini.dk.excel;

import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author fagarine
 */
public class MatrixExcelExporter extends AbstractExcelExporter {

    private String sheetName;

    private int[][] matrix;

    protected Workbook workbook;

    public MatrixExcelExporter(OutputStream output, String sheetName, int[][] matrix) {
        super(output);
        this.sheetName = sheetName;
        this.matrix = matrix;
    }

    @Override
    protected Workbook writeToWorkbook() {
        workbook = new HSSFWorkbook();
        Sheet sheet = initSheetAndHeader(workbook, sheetName);

        // 将数据放入sheet中
        if (matrix != null) {
            fillRows(sheet);
        }
        return workbook;
    }

    private void fillRows(Sheet sheet) {
        for (int index = 0; index < matrix.length; index++) {
            Row row = sheet.createRow(index + 1);
            int[] rowValue = matrix[index];
            if (rowValue == null) {
                continue;
            }

            try {
                for (int i = 0; i < 255; i++) {
                    Cell cell = row.createCell(i);
                    setCell(cell, rowValue[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void setCell(Cell cell, int value) {
        setCellValue(cell, value);
    }

    /**
     * 初始化excel表单和表头
     *
     * @param workbook 工作簿对象
     * @param sheetTitle 表单名称
     * @return 返回表单对象
     */
    private Sheet initSheetAndHeader(Workbook workbook, String sheetTitle) {
        // 生成一个表格
        Sheet sheet = workbook.createSheet(sheetTitle);
        // 设置表格默认列宽15个字节
        sheet.setDefaultColumnWidth(1);
        return sheet;
    }
}
