package cn.laoshini.dk.constant;

/**
 * 热修复执行结果枚举
 *
 * @author fagarine
 */
public enum HotfixResultEnum {

    /**
     * 执行成功
     */
    SUCCEED("执行成功"),

    /**
     * 类文件没有改变
     */
    NO_CHANGE("类文件没有改变"),

    /**
     * 未找到类
     */
    NO_CLASS("类未找到"),

    /**
     * 未找到agent
     */
    NO_AGENT("未找到agent"),

    /**
     * 执行出错
     */
    EXCEPTION("执行出错"),

    ;

    private String desc;

    HotfixResultEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
