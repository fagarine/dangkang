package cn.laoshini.dk.constant;

/**
 * @author fagarine
 */
public enum QueryConditionKeyEnum {

    /**
     * 数据库名
     */
    DB_NAME("DB_NAME", "数据库名"),
    TABLE_NAME("TABLE_NAME", "表名"),
    COLUMN_NAME("COLUMN_NAME", "列名"),
    UNIQUE_KEY("UNIQUE_KEY", "全表唯一标识"),
    ;

    private String key;

    private String cnName;

    QueryConditionKeyEnum(String key, String cnName) {
        this.key = key;
        this.cnName = cnName;
    }

    public String getKey() {
        return key;
    }

    public String getCnName() {
        return cnName;
    }

}
