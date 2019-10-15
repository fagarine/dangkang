package cn.laoshini.dk.constant;

/**
 * 行为树节点类型枚举
 *
 * @author fagarine
 */
public enum BtNodeType {

    /**
     * 根节点
     */
    ROOT(0, "root", "根节点") {
    },

    /**
     * 复合类型节点
     */
    COMPOSITE(1, "Composite Node", "复合类型节点") {
    },

    /**
     * 条件节点
     */
    CONDITION(2, "Condition Node", "条件节点") {
    },

    /**
     * 行为节点
     */
    ACTION(3, "Action Node", "行为节点") {
    },

    /**
     * 装饰类型节点
     */
    DECORATOR(4, "Decorator Node", "装饰类型节点") {
    },
    ;

    public static BtNodeType byCode(int nodeCode) {
        for (BtNodeType type : values()) {
            if (type.getCode() == nodeCode) {
                return type;
            }
        }
        return ROOT;
    }

    private int code;
    private String name;
    private String desc;

    private BtNodeType(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
