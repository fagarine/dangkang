package cn.laoshini.dk.excel.constraint;

import org.apache.poi.ss.usermodel.DataValidationConstraint;

import cn.laoshini.dk.exception.BusinessException;

/**
 * 数值型约束
 *
 * @author fagarine
 */
public class ExcelNumericHeader extends ExcelHeader {

    /**
     * 具体的数据验证类型，具体参见{@link DataValidationConstraint.ValidationType}
     */
    private int validationType;

    /**
     * 数据验证的比较方式，具体参见{@link DataValidationConstraint.OperatorType}
     */
    private int comparisonOperator;

    /**
     * 约束条件1的表达式
     */
    private String expr1;

    /**
     * 约束条件2的表达式
     */
    private String expr2;

    public ExcelNumericHeader(String name, int validationType, int comparisonOperator, String expr1, String expr2) {
        super(name);
        this.validationType = validationType;
        checkValidationType();

        this.comparisonOperator = comparisonOperator;
        checkComparisonOperator();
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    public ExcelNumericHeader(String name, boolean editable, int validationType, int comparisonOperator, String expr1,
            String expr2) {
        this(name, validationType, comparisonOperator, expr1, expr2);
        setEditable(editable);
    }

    private void checkComparisonOperator() {
        if (comparisonOperator < DataValidationConstraint.OperatorType.BETWEEN
                || comparisonOperator > DataValidationConstraint.OperatorType.LESS_OR_EQUAL) {
            throw new BusinessException("invalid.compare.type", "错误的excel数据验证比较类型:" + comparisonOperator);
        }
    }

    private void checkValidationType() {
        switch (validationType) {
            case DataValidationConstraint.ValidationType.INTEGER:
            case DataValidationConstraint.ValidationType.DECIMAL:
            case DataValidationConstraint.ValidationType.DATE:
            case DataValidationConstraint.ValidationType.TIME:
            case DataValidationConstraint.ValidationType.TEXT_LENGTH:
                break;

            default:
                throw new BusinessException("invalid.validation.type", "错误的excel数据验证类型:" + validationType);
        }
    }

    public boolean isDate() {
        return validationType == DataValidationConstraint.ValidationType.DATE;
    }

    public boolean isTime() {
        return validationType == DataValidationConstraint.ValidationType.TIME;
    }

    @Override
    public int getValidationType() {
        return validationType;
    }

    public int getComparisonOperator() {
        return comparisonOperator;
    }

    public String getExpr1() {
        return expr1;
    }

    public String getExpr2() {
        return expr2;
    }
}
