package cn.laoshini.dk.excel;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import cn.laoshini.dk.exception.BusinessException;

import static cn.laoshini.dk.excel.ExcelConstant.COLUMN_WIDTH;

/**
 * excel数据导出抽象类
 *
 * @author fagarine
 */
abstract class AbstractExcelExporter {

    protected OutputStream output;

    /**
     * 是否按照数据类型填充单元格，如果为false，数据将以字符串形式写入表格
     */
    protected boolean fillValueByType = true;

    public AbstractExcelExporter() {
    }

    public AbstractExcelExporter(OutputStream output) {
        this.output = output;
    }

    public AbstractExcelExporter(OutputStream output, boolean fillValueByType) {
        this.output = output;
        this.fillValueByType = fillValueByType;
    }

    /**
     * 导出excel数据到输出流中
     */
    public void export() {
        if (output == null) {
            throw new IllegalArgumentException("导出excel的输出流不能为空");
        }

        Workbook workbook = writeToWorkbook();
        try {
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            throw new BusinessException("excel.write.error", "向输出流写入excel数据出错");
        }
    }

    /**
     * 写入数据到Workbook并返回Workbook对象
     *
     * @return 该方法不会返回null
     */
    protected abstract Workbook writeToWorkbook();

    /**
     * 将数据放入sheet中
     *
     * @param sheet excel表单对象
     * @param rows 导出数据内容
     * @param <RowType> 单行数据对象类型，可以是JavaBean、List、数组
     */
    protected <RowType> void fillRows(Sheet sheet, List<RowType> rows) {
        for (int index = 0; index < rows.size(); index++) {
            Row row = sheet.createRow(index + 1);
            RowType rowValue = rows.get(index);
            if (rowValue == null) {
                continue;
            }

            if (rowValue.getClass().isArray()) {
                fillRowByArray(row, (Object[]) rowValue);
            } else if (rowValue instanceof List) {
                fillRowByList(row, (List) rowValue);
            } else {
                fillRowByJavaBean(row, rowValue);
            }
        }
    }

    /**
     * 向excel写入单行数据，数据以List形式传入
     *
     * @param row excel行对象
     * @param rowValue 单行数据
     */
    protected void fillRowByList(Row row, List rowValue) {
        if (rowValue == null || rowValue.isEmpty()) {
            return;
        }

        try {
            for (int index = 0; index < rowValue.size(); index++) {
                Cell cell = row.createCell(index);
                Object value = rowValue.get(index);
                setCellValue(cell, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向excel写入单行数据，数据以数组形式传入
     *
     * @param row excel中的行对象
     * @param rowValue 对应行的值
     */
    protected void fillRowByArray(Row row, Object[] rowValue) {
        if (rowValue == null || rowValue.length == 0) {
            return;
        }

        try {
            for (int index = 0; index < rowValue.length; index++) {
                Cell cell = row.createCell(index);
                Object value = rowValue[index];
                setCellValue(cell, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向excel写入单行数据，数据以JavaBean形式传入；利用反射，根据JavaBean属性的先后顺序，得到属性的值
     *
     * @param row excel中的行对象
     * @param rowValue 对应行的值
     */
    protected void fillRowByJavaBean(Row row, Object rowValue) {
        Field[] fields = rowValue.getClass().getDeclaredFields();
        try {
            for (int index = 0; index < fields.length; index++) {
                Cell cell = row.createCell(index);
                Field field = fields[index];
                boolean access = field.isAccessible();
                Object value = null;
                try {
                    field.setAccessible(true);
                    value = field.get(rowValue);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                } finally {
                    field.setAccessible(access);
                }

                setCellValue(cell, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setCellValue(Cell cell, Object value) {
        if (isFillValueByType()) {
            setCellValueByType(cell, value);
        } else {
            setCellStringValue(cell, value);
        }
    }

    protected void setCellStringValue(Cell cell, Object value) {
        String cellValue = value == null ? "" : String.valueOf(value);
        // 定义单元格为字符串类型
        cell.setCellType(CellType.STRING);
        cell.setCellValue(cellValue);
    }

    protected void setCellValueByType(Cell cell, Object value) {
        if (value == null) {
            cell.setCellType(CellType.BLANK);
        } else if (value instanceof Boolean) {
            cell.setCellType(CellType.BOOLEAN);
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Number) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Date) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue((Date) value);
        } else {
            cell.setCellType(CellType.STRING);
            cell.setCellValue(String.valueOf(value));
        }
    }

    protected CellStyle fillSheetStyle(Workbook workbook, HSSFSheet sheet) {
        // 设置表格默认列宽15个字节
        sheet.setDefaultColumnWidth(COLUMN_WIDTH);
        // 生成一个样式
        CellStyle style = defaultCellStyle(workbook);
        // 生成一个字体
        Font font = defaultFont(workbook);
        // 把字体应用到当前样式
        style.setFont(font);
        return style;
    }

    protected CellStyle createCellStyle(Workbook workbook, short bg) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(bg);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setLeftBorderColor(BorderStyle.THIN.getCode());
        style.setRightBorderColor(BorderStyle.THIN.getCode());
        style.setAlignment(HorizontalAlignment.CENTER);

        return style;
    }

    protected CellStyle defaultCellStyle(Workbook workbook) {
        return createCellStyle(workbook, HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
    }

    protected Font createFont(Workbook workbook, short color) {
        Font font = workbook.createFont();
        font.setColor(color);
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        return font;
    }

    protected Font defaultFont(Workbook workbook) {
        return createFont(workbook, HSSFColor.HSSFColorPredefined.WHITE.getIndex());
    }

    public boolean isFillValueByType() {
        return fillValueByType;
    }

    public void setFillValueByType(boolean fillValueByType) {
        this.fillValueByType = fillValueByType;
    }

    public OutputStream getOutput() {
        return output;
    }

    public void setOutput(OutputStream output) {
        this.output = output;
    }
}
