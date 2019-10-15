package cn.laoshini.dk.excel.constraint;

import org.apache.poi.ss.usermodel.DataValidationConstraint;

/**
 * 公式型
 *
 * @author fagarine
 */
public class ExcelFormulaHeader extends ExcelHeader {

    /**
     * 公式字符串
     */
    private String textExpr;

    public ExcelFormulaHeader(String name, String textExpr) {
        super(name);
        this.textExpr = textExpr;
    }

    public ExcelFormulaHeader(String name, boolean editable, String textExpr) {
        super(name, editable);
        this.textExpr = textExpr;
    }

    @Override
    public int getValidationType() {
        return DataValidationConstraint.ValidationType.FORMULA;
    }

    public String getTextExpr() {
        return textExpr;
    }
}
