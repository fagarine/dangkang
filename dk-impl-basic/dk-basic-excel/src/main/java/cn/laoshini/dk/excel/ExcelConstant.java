package cn.laoshini.dk.excel;

/**
 * excel导入导出功能相关常量
 *
 * @author fagarine
 */
public class ExcelConstant {
    private ExcelConstant() {
    }

    /**
     * excel缺省日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd";

    /**
     * 默认日期表达式，最小日期
     */
    public static final String MIN_DATE = "1900/01/01";

    /**
     * 默认日期表达式，最大日期
     */
    public static final String MAX_DATE = "2999/12/31";

    /**
     * 默认支持下拉列表的最大行数，从0开始计算
     */
    public static final int XLS_MAX_ROW = 65535;

    /**
     * 默认列宽
     */
    public static final int COLUMN_WIDTH = 15;
}
