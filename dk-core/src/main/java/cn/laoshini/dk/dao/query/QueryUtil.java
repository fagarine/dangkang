package cn.laoshini.dk.dao.query;

import java.util.LinkedHashMap;
import java.util.Map;

import cn.laoshini.dk.constant.QueryConditionKeyEnum;
import cn.laoshini.dk.domain.query.BeanQueryCondition;
import cn.laoshini.dk.domain.query.ListQueryCondition;

/**
 * @author fagarine
 */
public class QueryUtil {
    private QueryUtil() {
    }

    public static Map<String, Object> newFilters() {
        return new LinkedHashMap<>();
    }

    public static BeanQueryCondition newBeanQueryCondition() {
        BeanQueryCondition queryCondition = new BeanQueryCondition();
        queryCondition.setFilters(newFilters());
        return queryCondition;
    }

    public static BeanQueryCondition newBeanQueryCondition(Map<String, Object> filters) {
        BeanQueryCondition queryCondition = new BeanQueryCondition();
        queryCondition.setFilters(filters == null ? newFilters() : filters);
        return queryCondition;
    }

    public static BeanQueryCondition newBeanQueryCondition(Map<String, Object> filters, String tableName) {
        BeanQueryCondition queryCondition = new BeanQueryCondition();
        queryCondition.setFilters(filters == null ? newFilters() : filters);
        queryCondition.getFilters().put(QueryConditionKeyEnum.TABLE_NAME.getKey(), tableName);
        return queryCondition;
    }

    public static BeanQueryCondition newBeanQueryCondition(String key, Object value) {
        Map<String, Object> filters = newFilters();
        filters.put(key, value);
        return newBeanQueryCondition(filters);
    }

    public static BeanQueryCondition newBeanQueryCondition(String tableName, String key, Object value) {
        Map<String, Object> filters = newFilters();
        filters.put(QueryConditionKeyEnum.TABLE_NAME.getKey(), tableName);
        filters.put(key, value);
        return newBeanQueryCondition(filters);
    }

    public static ListQueryCondition newListQueryCondition() {
        ListQueryCondition queryCondition = new ListQueryCondition();
        queryCondition.setFilters(newFilters());
        return queryCondition;
    }

    public static ListQueryCondition newListQueryCondition(Map<String, Object> filters) {
        ListQueryCondition queryCondition = new ListQueryCondition();
        queryCondition.setFilters(filters == null ? newFilters() : filters);
        return queryCondition;
    }

    public static ListQueryCondition newListQueryCondition(Map<String, Object> filters, String tableName) {
        ListQueryCondition queryCondition = new ListQueryCondition();
        queryCondition.setFilters(filters == null ? newFilters() : filters);
        queryCondition.getFilters().put(QueryConditionKeyEnum.TABLE_NAME.getKey(), tableName);
        return queryCondition;
    }

    public static ListQueryCondition newListQueryCondition(String key, Object value) {
        Map<String, Object> filters = newFilters();
        filters.put(key, value);
        return newListQueryCondition(filters);
    }

    public static ListQueryCondition newListQueryCondition(String tableName) {
        Map<String, Object> filters = newFilters();
        filters.put(QueryConditionKeyEnum.TABLE_NAME.getKey(), tableName);
        return newListQueryCondition(filters);
    }

    public static ListQueryCondition newListQueryCondition(String tableName, String key, Object value) {
        Map<String, Object> filters = newFilters();
        filters.put(QueryConditionKeyEnum.TABLE_NAME.getKey(), tableName);
        filters.put(key, value);
        return newListQueryCondition(filters);
    }
}
