package cn.laoshini.dk.constant;

/**
 * @author fagarine
 */
public class ExpressionConstant {
    private ExpressionConstant() {
    }

    public static final String REQ_MESSAGE_PARAM = "req";

    public static final String REQ_MESSAGE_DATA = "reqData";

    public static final String GAME_SUBJECT_PARAM = "subject";

    public static final String RETURN_PARAM = "_ret";

    public static final String JS_DEPENDENCY_FUNCTION = "expressionDependencies()";

    /**
     * 表达式类型枚举
     */
    public enum ExpressionTypeEnum {
        /**
         * Spring表达式Spel
         */
        SPEL,
        /**
         * JavaScript脚本
         */
        JS,
        ;
    }

    /**
     * 表达式代码分类枚举
     */
    public enum ExpressionCodeType {
        /**
         * 逻辑代码
         */
        LOGIC,
        /**
         * 依赖声明
         */
        DEPENDENCY,
        ;
    }

}
