package cn.laoshini.dk.dao.update;

import java.util.Map;

/**
 * @author fagarine
 */
public class SqlWrapper {

    protected String tableName;

    protected Class<?> entityType;

    protected Map<String, Object> condition;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    public void setEntityType(Class<?> entityType) {
        this.entityType = entityType;
    }

    public Map<String, Object> getCondition() {
        return condition;
    }

    public void setCondition(Map<String, Object> condition) {
        this.condition = condition;
    }
}
