package cn.laoshini.dk.domain.query;

import java.util.Map;

/**
 * @author fagarine
 */
public abstract class AbstractQueryCondition {

    protected Map<String, Object> filters;

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    @Override
    public String toString() {
        return "AbstractQueryCondition{" + "filters=" + filters + '}';
    }
}
