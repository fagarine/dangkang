package cn.laoshini.dk.dao.update;

import java.util.Map;

/**
 * @author fagarine
 */
public class UpdateSqlWrapper extends SqlWrapper {

    protected Map<String, Object> updateValues;

    public Map<String, Object> getUpdateValues() {
        return updateValues;
    }

    public void setUpdateValues(Map<String, Object> updateValues) {
        this.updateValues = updateValues;
    }
}
