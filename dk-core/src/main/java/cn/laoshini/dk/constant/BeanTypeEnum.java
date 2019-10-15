package cn.laoshini.dk.constant;

import java.util.HashMap;
import java.util.Map;

import cn.laoshini.dk.exception.BusinessException;

/**
 * 基础Bean类型枚举
 *
 * @author fagarine
 */
public enum BeanTypeEnum {

    /**
     * 基础Bean类型枚举
     */
    BOOLEAN(1, "布尔值"),

    BYTE(2, "byte型数值"),

    SHORT(3, "short型数值"),

    INTEGER(4, "int型数值"),

    LONG(5, "long型数值"),

    DOUBLE(6, "浮点数"),

    STRING(7, "字符串表达式"),

    ORDINARY(8, "JavaBean"),

    LIST(9, "集合、数组"),

    COMPOSITE(10, "复合类型（BeanTypeEnum类型数据的集合）"),
    ;

    private static final Map<Integer, BeanTypeEnum> TYPE_CODE_MAP = new HashMap<>(values().length);

    static {
        for (BeanTypeEnum typeEnum : values()) {
            TYPE_CODE_MAP.put(typeEnum.code, typeEnum);
        }
    }

    public static BeanTypeEnum codeOf(int code) {
        BeanTypeEnum typeEnum = TYPE_CODE_MAP.get(code);
        if (typeEnum == null) {
            throw new BusinessException("bean.type.error", String.format("未找到编码为%d的Bean类型", code));
        }
        return typeEnum;
    }

    private int code;

    private String description;

    BeanTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
