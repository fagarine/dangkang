package cn.laoshini.dk.excel.constraint;

import cn.laoshini.dk.excel.ExcelConstant;

/**
 * 日期约束
 *
 * @author fagarine
 */
public class ExcelDateHeader extends ExcelNumericHeader {

    /**
     * 日期格式字符串
     */
    private String dateFormat;

    public ExcelDateHeader(String name, int validationType, int comparisonOperator, String expr1, String expr2,
            String dateFormat) {
        super(name, validationType, comparisonOperator, expr1, expr2);
        this.dateFormat = dateFormat;
    }

    public ExcelDateHeader(String name, boolean editable, int validationType, int comparisonOperator, String expr1,
            String expr2, String dateFormat) {
        super(name, editable, validationType, comparisonOperator, expr1, expr2);
        this.dateFormat = dateFormat;
    }

    public String getDateFormat() {
        if (dateFormat == null) {
            dateFormat = ExcelConstant.DEFAULT_DATE_FORMAT;
        }
        return dateFormat;
    }
}
