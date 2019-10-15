package cn.laoshini.dk.excel.constraint;

import org.apache.poi.ss.usermodel.DataValidationConstraint;

import cn.laoshini.dk.excel.ConstraintExcelExporter;

import static cn.laoshini.dk.excel.ExcelConstant.MAX_DATE;
import static cn.laoshini.dk.excel.ExcelConstant.MIN_DATE;

/**
 * excel表单表头信息，包括列名，约束条件等
 *
 * @author fagarine
 */
public class ExcelHeader {

    /**
     * 列标题名称
     */
    private String name;

    /**
     * 记录本列数据是否可以被编辑，默认为true；
     * 该功能须与{@link ConstraintExcelExporter#protection} 联合使用，当{@link ConstraintExcelExporter#protection}为true时，
     * 所有列中，只要有列的该值设置为false，会导致整个excel表单所有单元格锁定，除了导出内容中可编辑的单元格外，其他单元格都不可编辑
     * 如：导出一个包含10行、10列数据的表格，如果某些列设置为不可编辑，
     * 则除了这导出的10行数据中可编辑列的单元格外，其他所有单元格（包括外面的空白单元格）都不可编辑
     * 具体逻辑见: {@link ConstraintExcelExporter#isLockedCell(ExcelHeader)}
     */
    private boolean editable = true;

    /**
     * 是否自适应列宽，默认为false
     */
    private boolean autoSize;

    public ExcelHeader(String name) {
        this.name = name;
    }

    public ExcelHeader(String name, boolean editable) {
        this.name = name;
        this.editable = editable;
    }

    /**
     * 返回excel单元格数据验证类型
     *
     * @return 返回类型
     */
    public int getValidationType() {
        return DataValidationConstraint.ValidationType.ANY;
    }

    public static ExcelHeader newHeader(String name) {
        return new ExcelHeader(name);
    }

    public static ExcelHeader lockedColumnHeader(String name) {
        return new ExcelHeader(name, false);
    }

    public static ExcelListHeader listHeader(String name, String[] options) {
        return new ExcelListHeader(name, options);
    }

    public static ExcelListHeader lockedListColumnHeader(String name, String[] options) {
        return new ExcelListHeader(name, false, options);
    }

    public static ExcelFormulaHeader formulaHeader(String name, String textExpr) {
        return new ExcelFormulaHeader(name, textExpr);
    }

    public static ExcelFormulaHeader lockedFormulaColumnHeader(String name, String textExpr) {
        return new ExcelFormulaHeader(name, false, textExpr);
    }

    public static ExcelNumericHeader createNumericHeader(String name, int validationType, int comparisonOperator,
            String expr1, String expr2) {
        return new ExcelNumericHeader(name, validationType, comparisonOperator, expr1, expr2);
    }

    public static ExcelNumericHeader lockedNumericColumnHeader(String name, int validationType, int comparisonOperator,
            String expr1, String expr2) {
        return new ExcelNumericHeader(name, false, validationType, comparisonOperator, expr1, expr2);
    }

    public static ExcelNumericHeader createNumericHeader(String name, int validationType, String expr1, String expr2) {
        return createNumericHeader(name, validationType, DataValidationConstraint.OperatorType.BETWEEN, expr1, expr2);
    }

    public static ExcelNumericHeader lockedNumericColumnHeader(String name, int validationType, String expr1,
            String expr2) {
        return lockedNumericColumnHeader(name, validationType, DataValidationConstraint.OperatorType.BETWEEN, expr1,
                expr2);
    }

    public static ExcelNumericHeader integerHeader(String name) {
        return createNumericHeader(name, DataValidationConstraint.ValidationType.INTEGER,
                String.valueOf(Integer.MIN_VALUE), String.valueOf(Integer.MAX_VALUE));
    }

    public static ExcelNumericHeader lockedIntegerColumnHeader(String name) {
        return lockedNumericColumnHeader(name, DataValidationConstraint.ValidationType.INTEGER,
                String.valueOf(Integer.MIN_VALUE), String.valueOf(Integer.MAX_VALUE));
    }

    public static ExcelNumericHeader integerHeader(String name, int min, int max) {
        return createNumericHeader(name, DataValidationConstraint.ValidationType.INTEGER, String.valueOf(min),
                String.valueOf(max));
    }

    public static ExcelNumericHeader decimalHeader(String name) {
        return createNumericHeader(name, DataValidationConstraint.ValidationType.DECIMAL,
                String.valueOf(Double.MIN_VALUE), String.valueOf(Double.MAX_VALUE));
    }

    public static ExcelNumericHeader lockedDecimalColumnHeader(String name) {
        return lockedNumericColumnHeader(name, DataValidationConstraint.ValidationType.DECIMAL,
                String.valueOf(Double.MIN_VALUE), String.valueOf(Double.MAX_VALUE));
    }

    public static ExcelNumericHeader decimalHeader(String name, double min, double max) {
        return createNumericHeader(name, DataValidationConstraint.ValidationType.DECIMAL, String.valueOf(min),
                String.valueOf(max));
    }

    public static ExcelNumericHeader lengthLimitHeader(String name, int minLen, int maxLen) {
        return createNumericHeader(name, DataValidationConstraint.ValidationType.TEXT_LENGTH, String.valueOf(minLen),
                String.valueOf(maxLen));
    }

    public static ExcelDateHeader dateHeader(String name, String minExpr, String maxExpr, String dateFormat) {
        return new ExcelDateHeader(name, DataValidationConstraint.ValidationType.DATE,
                DataValidationConstraint.OperatorType.BETWEEN, minExpr, maxExpr, dateFormat);
    }

    public static ExcelDateHeader lockedDateColumnHeader(String name, String minExpr, String maxExpr,
            String dateFormat) {
        return new ExcelDateHeader(name, false, DataValidationConstraint.ValidationType.DATE,
                DataValidationConstraint.OperatorType.BETWEEN, minExpr, maxExpr, dateFormat);
    }

    /**
     * 使用默认日期格式（"yyyy/MM/dd"）
     *
     * @param name 列标题
     * @param minExpr 最小限制日期字符串
     * @param maxExpr 最大限制日期字符串
     * @return
     */
    public static ExcelDateHeader dateHeader(String name, String minExpr, String maxExpr) {
        return dateHeader(name, minExpr, maxExpr, null);
    }

    public static ExcelDateHeader lockedDateColumnHeader(String name, String minExpr, String maxExpr) {
        return lockedDateColumnHeader(name, minExpr, maxExpr, null);
    }

    public static ExcelDateHeader dateHeader(String name) {
        return dateHeader(name, MIN_DATE, MAX_DATE);
    }

    public static ExcelDateHeader lockedDateColumnHeader(String name) {
        return lockedDateColumnHeader(name, MIN_DATE, MAX_DATE);
    }

    public static ExcelNumericHeader timeHeader(String name, String minExpr, String maxExpr) {
        return createNumericHeader(name, DataValidationConstraint.ValidationType.TIME, minExpr, maxExpr);
    }

    public static ExcelNumericHeader timeHeader(String name) {
        return timeHeader(name, "00:00:00", "23:59:59");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isAutoSize() {
        return autoSize;
    }

    public void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
    }
}
