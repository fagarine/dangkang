package cn.laoshini.dk.constant;

/**
 * 行为树复合节点类型枚举
 *
 * @author fagarine
 */
public enum CompositeType {

    /**
     * 非复合类型节点
     */
    NONE(0, "Not Composite Node") {
    },

    /**
     * 选择执行，当执行本类型Node时，它将从begin到end迭代执行自己的Child Node: <br/>
     * 如遇到一个Child Node执行后返回True，将停止迭代，本Node向自己的Parent Node也返回True；<br/>
     * 否则所有Child Node都返回False， 那本Node向自己的Parent Node返回False。
     */
    SELECTOR(1, "Selector Node") {
    },

    /**
     * 顺序执行，当执行本类型Node时，它将从begin到end迭代执行自己的Child Node:<br/>
     * 如遇到一个Child Node执行后返回False，则停止迭代本Node向自己的Parent Node也返回False；<br/>
     * 否则所有Child Node都返回True， 那本Node向自己的Parent Node返回True。
     */
    SEQUENCE(2, "Sequence Node") {
    },

    /** 以下为Parallel Node类型的节点，基本特征：平行执行它的所有Child Node **/

    /**
     * 一个Child Node返回False则向自己的Parent Node返回False，全True才返回True。
     */
    PARALLEL_SELECTOR(3, "Parallel Selector Node") {
    },

    /**
     * 一个Child Node返回True则向自己的Parent Node返回True，全False才返回False。
     */
    PARALLEL_SEQUENCE(4, "Parallel Sequence Node") {
    },

    /**
     * 所有Child Node返回False才向自己的Parent Node返回False，否则返回True。
     */
    PARALLEL_FAIL_ON_ALL(5, "Parallel Fail On All Node") {
    },

    /**
     * 所有Child Node返回True才向自己的Parent Node返回True，否则返回False。
     */
    PARALLEL_SUCC_ON_ALL(6, "Parallel Succeed On All Node") {
    },

    /**
     * 指定数量的Child Node返回False后才向自己的Parent Node返回False。
     */
    PARALLEL_HYBIRD_FAIL(7, "Parallel Hybird Fail Node") {
    },

    /**
     * 指定数量的Child Node返回True后才向自己的Parent Node返回True。
     */
    PARALLEL_HYBIRD_SUCC(8, "Parallel Hybird Succeed Node") {
    },
    ;

    public static CompositeType valueOf(int type) {
        for (CompositeType compositeType : values()) {
            if (compositeType.type == type) {
                return compositeType;
            }
        }
        return NONE;
    }

    private int type;
    private String name;

    CompositeType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
