package cn.laoshini.dk.excel;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;

import cn.laoshini.dk.excel.constraint.ExcelDateHeader;
import cn.laoshini.dk.excel.constraint.ExcelHeader;
import cn.laoshini.dk.excel.constraint.ExcelListHeader;
import cn.laoshini.dk.excel.constraint.ExcelNumericHeader;

import static cn.laoshini.dk.excel.ExcelConstant.XLS_MAX_ROW;

/**
 * excel数据导出工具，包含约束条件
 *
 * @param <RowType> 单行数据对象类型，可以是JavaBean、List、数组
 * @author fagarine
 */
public class ConstraintExcelExporter<RowType> extends AbstractExcelExporter {

    private String sheetTitle;

    /**
     * excel表单表头信息，包括列名，约束条件等
     */
    private List<ExcelHeader> headers;

    private List<RowType> rows;

    /**
     * 导出表单是否受保护，如果为true，则只有指定可编辑的单元格才可以编辑
     */
    private boolean protection = false;

    private HSSFWorkbook workbook;

    private HSSFSheet sheet;

    /**
     * 可编辑单元格的样式
     */
    private CellStyle editableStyle;

    /**
     * 锁定单元格（不可编辑）的样式
     */
    private CellStyle lockStyle;

    /**
     * 记录日期类单元格的样式，数组长度与列长度一致，通过列索引获取对应的日期单元格样式
     */
    private CellStyle[] dateCellStyles;

    public ConstraintExcelExporter(OutputStream output, String sheetTitle, List<ExcelHeader> headers,
            List<RowType> rows) {
        super(output);
        this.sheetTitle = sheetTitle;
        this.headers = headers;
        this.rows = rows;
    }

    public ConstraintExcelExporter(OutputStream output, String sheetTitle, List<ExcelHeader> headers,
            List<RowType> rows, boolean protection) {
        super(output);
        this.sheetTitle = sheetTitle;
        this.headers = headers;
        this.rows = rows;
        this.protection = protection;
    }

    public ConstraintExcelExporter(OutputStream output, String sheetTitle, List<ExcelHeader> headers,
            List<RowType> rows, boolean fillValueByType, boolean protection) {
        super(output, fillValueByType);
        this.sheetTitle = sheetTitle;
        this.headers = headers;
        this.rows = rows;
        this.protection = protection;
    }

    @Override
    protected Workbook writeToWorkbook() {
        workbook = new HSSFWorkbook();
        initSheetAndHeader();

        // 填充数据
        if (rows == null || rows.isEmpty()) {
            return workbook;
        }

        // 将数据放入sheet中
        fillRows(sheet, rows);

        // 列宽自适应处理，只处理设置了自适应的列
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).isAutoSize()) {
                sheet.autoSizeColumn(i);
            }
        }

        if (protection) {
            sheet.protectSheet(new Date().toString());
        }

        return workbook;
    }

    private void initSheetAndHeader() {
        // 生成一个表格
        sheet = workbook.createSheet(sheetTitle);

        // 生成并填写表头内容
        fillSheetHeaders();
    }

    /**
     * 生成并填写表头内容
     */
    private void fillSheetHeaders() {
        //生成表格标题
        HSSFRow firstRow = sheet.createRow(0);
        firstRow.setHeight((short) 300);

        if (headers == null || headers.size() == 0) {
            return;
        }

        CellStyle headerStyle = fillSheetStyle(workbook, sheet);
        headerStyle.setLocked(true);
        lockStyle = workbook.createCellStyle();
        lockStyle.setLocked(true);
        lockStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        lockStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        editableStyle = workbook.createCellStyle();
        editableStyle.setLocked(false);

        HSSFCell cell;
        ExcelHeader header;
        for (int i = 0; i < headers.size(); i++) {
            header = headers.get(i);
            cell = firstRow.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(header.getName());
            cell.setCellValue(text);
            cell.setCellStyle(headerStyle);

            DVConstraint constraint;
            switch (header.getValidationType()) {
                case DataValidationConstraint.ValidationType.INTEGER:
                case DataValidationConstraint.ValidationType.DECIMAL:
                case DataValidationConstraint.ValidationType.DATE:
                case DataValidationConstraint.ValidationType.TIME:
                case DataValidationConstraint.ValidationType.TEXT_LENGTH:
                    ExcelNumericHeader numericHeader = (ExcelNumericHeader) header;
                    if (numericHeader.isDate()) {
                        ExcelDateHeader dateHeader = (ExcelDateHeader) numericHeader;
                        constraint = DVConstraint
                                .createDateConstraint(dateHeader.getComparisonOperator(), dateHeader.getExpr1(),
                                        dateHeader.getExpr2(), dateHeader.getDateFormat());

                        CellStyle dateCellStyle = workbook.createCellStyle();
                        if (isLockedCell(header)) {
                            dateCellStyle.cloneStyleFrom(lockStyle);
                        } else {
                            dateCellStyle.cloneStyleFrom(editableStyle);
                        }
                        HSSFDataFormat format = workbook.createDataFormat();
                        dateCellStyle.setDataFormat(format.getFormat(dateHeader.getDateFormat()));
                        setDateCellStyle(i, dateCellStyle);
                    } else if (numericHeader.isTime()) {
                        constraint = DVConstraint
                                .createTimeConstraint(numericHeader.getComparisonOperator(), numericHeader.getExpr1(),
                                        numericHeader.getExpr2());
                    } else {
                        constraint = DVConstraint.createNumericConstraint(header.getValidationType(),
                                numericHeader.getComparisonOperator(), numericHeader.getExpr1(),
                                numericHeader.getExpr2());
                    }
                    break;

                case DataValidationConstraint.ValidationType.LIST:
                    constraint = DVConstraint.createExplicitListConstraint(((ExcelListHeader) header).getOptions());
                    break;

                case DataValidationConstraint.ValidationType.ANY:
                case DataValidationConstraint.ValidationType.FORMULA:
                default:
                    constraint = null;
                    break;
            }

            if (constraint != null) {
                CellRangeAddressList addressList = new CellRangeAddressList(1, XLS_MAX_ROW, i, i);
                // 数据有效性对象
                sheet.addValidationData(new HSSFDataValidation(addressList, constraint));
            }
        }
    }

    /**
     * 判断单元格是否需要锁定，只有在表单受保护，且列设置为不可编辑时，才锁定单元格
     *
     * @param header 列描述信息
     * @return 返回判断结果
     */
    private boolean isLockedCell(ExcelHeader header) {
        return protection && !header.isEditable();
    }

    private void setDateCellStyle(int index, CellStyle dateCellStyle) {
        if (dateCellStyles == null) {
            dateCellStyles = new CellStyle[headers.size()];
        }

        dateCellStyles[index] = dateCellStyle;
    }

    @Override
    public void setCellValueByType(Cell cell, Object value) {
        int index = cell.getColumnIndex();
        if (index < headers.size()) {
            ExcelHeader header = headers.get(cell.getColumnIndex());
            if (header instanceof ExcelDateHeader) {
                cell.setCellStyle(dateCellStyles[index]);
            } else {
                if (isLockedCell(header)) {
                    cell.setCellStyle(lockStyle);
                } else {
                    cell.setCellStyle(editableStyle);
                }
            }
        }

        super.setCellValueByType(cell, value);
    }

    public boolean isProtection() {
        return protection;
    }

    public void setProtection(boolean protection) {
        this.protection = protection;
    }
}
