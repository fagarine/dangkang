package cn.laoshini.dk.dao;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.time.DateFormatUtils;

import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.constant.DBTypeEnum;
import cn.laoshini.dk.constant.QueryConditionKeyEnum;
import cn.laoshini.dk.domain.query.AbstractQueryCondition;
import cn.laoshini.dk.domain.query.PageQueryCondition;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.util.ReflectHelper;
import cn.laoshini.dk.util.StringUtil;
import cn.laoshini.dk.util.TypeUtil;

/**
 * SQL构建工具类，使用字符串拼接，未使用PreparedStatement
 *
 * @author fagarine
 */
public class SqlBuilder {
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 单引号
     */
    public static final String QUOTE = Constants.SINGLE_QUOTES;
    /**
     * 半角逗号
     */
    public static final String COMMA = Constants.SEPARATOR_COMMA;
    private static final Set<String> SIMPLE_TYPE_SET = new HashSet<>();
    private static final Map<Class<?>, Integer> JDBC_TYPE_MAP = new HashMap<>();
    /**
     * 当前系统中使用的数据库类型
     */
    private static DBTypeEnum dbType;

    static {
        SIMPLE_TYPE_SET.add(boolean.class.getName());
        SIMPLE_TYPE_SET.add(Boolean.class.getName());
        SIMPLE_TYPE_SET.add(byte.class.getName());
        SIMPLE_TYPE_SET.add(Byte.class.getName());
        SIMPLE_TYPE_SET.add(short.class.getName());
        SIMPLE_TYPE_SET.add(Short.class.getName());
        SIMPLE_TYPE_SET.add(int.class.getName());
        SIMPLE_TYPE_SET.add(Integer.class.getName());
        SIMPLE_TYPE_SET.add(long.class.getName());
        SIMPLE_TYPE_SET.add(Long.class.getName());
        SIMPLE_TYPE_SET.add(float.class.getName());
        SIMPLE_TYPE_SET.add(Float.class.getName());
        SIMPLE_TYPE_SET.add(double.class.getName());
        SIMPLE_TYPE_SET.add(Double.class.getName());
        SIMPLE_TYPE_SET.add(BigDecimal.class.getName());

        JDBC_TYPE_MAP.put(Boolean.TYPE, Types.BIT);
        JDBC_TYPE_MAP.put(Boolean.class, Types.BIT);
        JDBC_TYPE_MAP.put(Byte.TYPE, Types.BIT);
        JDBC_TYPE_MAP.put(Byte.class, Types.BIT);
        JDBC_TYPE_MAP.put(Integer.TYPE, Types.INTEGER);
        JDBC_TYPE_MAP.put(Integer.class, Types.INTEGER);
        JDBC_TYPE_MAP.put(Long.TYPE, Types.BIGINT);
        JDBC_TYPE_MAP.put(Long.class, Types.BIGINT);
        JDBC_TYPE_MAP.put(Float.TYPE, Types.FLOAT);
        JDBC_TYPE_MAP.put(Float.class, Types.FLOAT);
        JDBC_TYPE_MAP.put(Double.TYPE, Types.DOUBLE);
        JDBC_TYPE_MAP.put(Double.class, Types.DOUBLE);
        JDBC_TYPE_MAP.put(byte[].class, Types.BLOB);
        JDBC_TYPE_MAP.put(Byte[].class, Types.BLOB);
        JDBC_TYPE_MAP.put(BigDecimal.class, Types.DECIMAL);
        JDBC_TYPE_MAP.put(String.class, Types.VARCHAR);
        JDBC_TYPE_MAP.put(Date.class, Types.TIMESTAMP);
        JDBC_TYPE_MAP.put(java.sql.Date.class, Types.TIMESTAMP);
        JDBC_TYPE_MAP.put(java.sql.Time.class, Types.TIME);
        JDBC_TYPE_MAP.put(java.sql.Timestamp.class, Types.TIMESTAMP);
    }

    private SqlBuilder() {
    }

    public static int getJdbcType(Object bean) {
        if (bean == null) {
            return Types.VARCHAR;
        } else if (bean instanceof Class) {
            return toJdbcType((Class<?>) bean);
        } else {
            return toJdbcType(bean.getClass());
        }
    }

    public static int toJdbcType(Class<?> clazz) {
        if (JDBC_TYPE_MAP.containsKey(clazz)) {
            return JDBC_TYPE_MAP.get(clazz);
        }
        return Types.VARCHAR;
    }

    /**
     * 将实体类字段字段名称，转换为对应的表字段的名称
     *
     * @param entityType 实体类
     * @param fieldName 类字段的名称
     * @return 返回表字段名称
     */
    public static String toColumnName(Class<?> entityType, String fieldName) {
        if (entityType != null && entityType.isAnnotationPresent(TableMapping.class)) {
            TableMapping annotation = entityType.getAnnotation(TableMapping.class);
            if (annotation.columnFormat().equals(annotation.fieldFormat())) {
                return fieldName;
            } else {
                return annotation.fieldFormat().to(annotation.columnFormat(), fieldName);
            }
        }
        return fieldName;
    }

    /**
     * 转换实体类中，指定表字段对应的类字段名
     *
     * @param entityType 实体类
     * @param columnName 表中字段名称
     * @return 返回类字段名
     */
    public static String toFieldName(Class<?> entityType, String columnName) {
        if (entityType != null && entityType.isAssignableFrom(TableMapping.class)) {
            TableMapping annotation = entityType.getAnnotation(TableMapping.class);
            if (annotation.columnFormat().equals(annotation.fieldFormat())) {
                return columnName;
            } else {
                return annotation.columnFormat().to(annotation.fieldFormat(), columnName);
            }
        }
        return columnName;
    }

    /**
     * 拼接SQL字符串并返回
     *
     * @param tableName 表名称
     * @param bean 待保存对象
     * @return 返回拼接后的SQL
     */
    public static String buildInsertSql(String tableName, Object bean) {
        StringBuilder insertSql = new StringBuilder("INSERT INTO `").append(tableName).append("` (");
        StringBuilder fieldStr = new StringBuilder();
        StringBuilder valueStr = new StringBuilder();

        Field[] fields = bean.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                fieldStr.append(COMMA);
                valueStr.append(COMMA);
            }

            Field field = fields[i];
            fieldStr.append("`").append(field.getName()).append("`");
            valueStr.append(getFieldSqlValue(bean, field));
        }

        return insertSql.append(fieldStr).append(") values (").append(valueStr).append(")").toString();
    }

    public static String buildInsertSql(Object bean) {
        return buildInsertSql(getTableName(bean), bean);
    }

    public static <T> String buildBatchInsertSql(String tableName, List<T> beans) {
        if (beans == null || beans.isEmpty()) {
            return null;
        }

        StringBuilder insertSql = new StringBuilder("INSERT INTO `").append(tableName).append("` (");
        StringBuilder fieldStr = new StringBuilder();
        StringBuilder valueStr = new StringBuilder();

        Field[] fields = beans.get(0).getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                fieldStr.append(COMMA);
            }

            Field field = fields[i];
            fieldStr.append("`").append(field.getName()).append("`");
        }

        for (T bean : beans) {
            if (valueStr.length() > 0) {
                valueStr.append(COMMA);
            }
            valueStr.append(" (");
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) {
                    valueStr.append(COMMA);
                }

                Field field = fields[i];
                valueStr.append(getFieldSqlValue(bean, field));
            }
            valueStr.append(" )");
        }

        return insertSql.append(fieldStr).append(") values (").append(valueStr).append(")").toString();
    }

    public static String buildUpdateSql(String tableName, Object bean, List<String> keys) {
        StringBuilder updateSql = new StringBuilder("UPDATE `").append(tableName).append("` SET ");
        StringBuilder updateStr = new StringBuilder(128);
        StringBuilder conditionStr = new StringBuilder(128);

        Field[] fields = bean.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                updateStr.append(COMMA);
            }

            Field field = fields[i];
            if (keys.contains(field.getName())) {
                if (conditionStr.length() > 0) {
                    conditionStr.append(" AND ");
                }
                conditionStr.append("`").append(field.getName()).append("`=").append(getFieldSqlValue(bean, field));
            } else {
                updateStr.append("`").append(field.getName()).append("`=").append(getFieldSqlValue(bean, field));
            }
        }

        return updateSql.append(updateStr).append(" WHERE ").append(conditionStr).toString();
    }

    public static String buildUpdateSql(Object bean) {
        return buildUpdateSql(getTableName(bean), bean, getTableKeyFields(bean));
    }

    public static String buildUpdateSql(String tableName, Object bean, String... keys) {
        List<String> keyList;
        if (keys != null && keys.length > 0) {
            keyList = new ArrayList<>(keys.length);
            Collections.addAll(keyList, keys);
        } else {
            keyList = Collections.emptyList();
        }
        return buildUpdateSql(tableName, bean, keyList);
    }

    public static String buildSelectSql(AbstractQueryCondition condition) {
        Map<String, Object> filters = condition.getFilters();
        String tableName = (String) filters.remove(QueryConditionKeyEnum.TABLE_NAME.getKey());
        StringBuilder selectSql = new StringBuilder("SELECT * FROM `").append(tableName).append("` ");
        if (!filters.isEmpty()) {
            selectSql.append(toSelectWhereSql(filters));
        }
        return selectSql.toString();
    }

    private static StringBuilder toSelectWhereSql(Map<String, Object> filters) {
        StringBuilder conditionStr = new StringBuilder();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            if (conditionStr.length() > 0) {
                conditionStr.append(" AND ");
            } else {
                conditionStr.append(" WHERE ");
            }
            conditionStr.append("`").append(entry.getKey()).append("`=").append(toSqlValue(entry.getValue()));
        }
        return conditionStr;
    }

    public static String buildPageQuerySql(PageQueryCondition condition) {
        Map<String, Object> filters = condition.getFilters();
        String tableName = (String) filters.remove(QueryConditionKeyEnum.TABLE_NAME.getKey());
        StringBuilder conditionStr = toSelectWhereSql(filters);
        StringBuilder orderByStr = new StringBuilder();

        if (StringUtil.isNotEmptyString(condition.getOrderBy())) {
            orderByStr.append(" ORDER BY `").append(condition.getOrderBy()).append("` ");
            if (StringUtil.isNotEmptyString(condition.getOrderSort())) {
                orderByStr.append(condition.getOrderSort());
            }
        }

        return appendPageSql(tableName, condition, conditionStr, orderByStr);
    }

    static String appendPageSql(String tableName, PageQueryCondition condition, StringBuilder conditionStr,
            StringBuilder orderByStr) {
        StringBuilder selectSql = new StringBuilder(128);
        int startOffset = condition.getStartOffset();
        int pageSize = condition.getPageSize();
        int end = startOffset + pageSize;
        switch (getDBType()) {
            case MYSQL:
                selectSql.append("SELECT * FROM `").append(tableName).append("` ").append(conditionStr)
                        .append(orderByStr).append(" LIMIT ").append(startOffset).append(COMMA).append(pageSize);
                break;
            case SQL_SERVER:
                selectSql.append("SELECT `").append(tableName).append("`.* FROM (SELECT ROW_NUMBER() OVER(")
                        .append(orderByStr).append(") as rn, * FROM `").append(tableName).append("`) ")
                        .append(conditionStr).append(" AND rn >").append(startOffset).append(" AND rn <=").append(end);
                break;
            case ORACLE:
                selectSql.append("SELECT `").append(tableName).append("`.* FROM (SELECT ROWNUM rn, * FROM `")
                        .append(tableName).append("`) ").append(conditionStr).append(" AND rn <=").append(end)
                        .append(" AND rn >").append(startOffset).append(orderByStr);
                break;
            case DB2:
                selectSql.append("SELECT `").append(tableName).append("`.* FROM (SELECT ROW_NUMBER() OVER(")
                        .append(orderByStr).append(") as rn, * FROM `").append(tableName).append("`) ")
                        .append(conditionStr).append(" AND rn BETWEEN ").append(startOffset).append(" AND ")
                        .append(end);
                break;
            default:
                break;
        }
        return selectSql.toString();
    }

    public static String buildValidateTableSql(String tableName) {
        return String.format("SELECT 1 FROM `%s` WHERE 1=1", tableName);
    }

    public static String buildCountSql(String tableName) {
        return String.format("SELECT COUNT(1) FROM `%s`", tableName);
    }

    public static DBTypeEnum getDBType() {
        if (dbType == null) {
            String dbDriver = SpringContextHolder.getProperty("dk.rdb.driver");
            initDbType(dbDriver);
        }
        return dbType;
    }

    public static void initDbType(String dbDriver) {
        if ("com.mysql.cj.jdbc.Driver".equals(dbDriver) || "com.mysql.jdbc.Driver".equals(dbDriver)) {
            // Mysql
            dbType = DBTypeEnum.MYSQL;
        } else if ("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(dbDriver)) {
            // SQLServer 2005
            dbType = DBTypeEnum.SQL_SERVER;
        } else if ("oracle.jdbc.driver.OracleDriver".equals(dbDriver)) {
            // oracle
            dbType = DBTypeEnum.ORACLE;
        } else if ("com.ibm.db2.jdbc.app.DB2Driver".equals(dbDriver)) {
            // db2
            dbType = DBTypeEnum.DB2;
        } else {
            throw new BusinessException("db.type.unknown", "系统不支持的数据库类型:" + dbDriver);
        }
    }

    private static String getFieldSqlValue(Object bean, Field field) {
        String value;
        boolean access = field.isAccessible();
        try {
            field.setAccessible(true);
            value = toSqlValue(field.get(bean));
        } catch (IllegalAccessException e) {
            throw new BusinessException("field.access.fail", "");
        } finally {
            field.setAccessible(access);
        }
        return value;
    }

    static List<String> getTableKeyFields(Object bean) {
        return bean == null || TypeUtil.isNormalType(bean.getClass()) ? null : getTableKeyFields(bean.getClass());
    }

    static List<String> getTableKeyFields(Class<?> entityType) {
        List<String> keys = new ArrayList<>();
        Field[] fields = entityType.getDeclaredFields();
        for (Field field : fields) {
            TableKey key = field.getAnnotation(TableKey.class);
            if (key != null) {
                keys.add(field.getName());
            }
        }
        return keys;
    }

    private static String getTableName(Object bean) {
        TableMapping annotation = bean.getClass().getAnnotation(TableMapping.class);
        if (annotation == null) {
            throw new BusinessException("not.table.mapping", "未找到实体类对应的数据库表名");
        }

        return ReflectHelper.getTableMappingName(annotation, bean.getClass().getSimpleName());
    }

    private static String toSqlValue(Object val) {
        if (val == null) {
            return "null";
        }

        if (val instanceof Date) {
            return QUOTE + DateFormatUtils.format((Date) val, DEFAULT_DATE_FORMAT) + QUOTE;
        } else if (val instanceof String) {
            return QUOTE + String.valueOf(val).replace("\r", "").replaceAll(QUOTE, "") + QUOTE;
        }

        Class<?> clazz = val.getClass();
        if (SIMPLE_TYPE_SET.contains(clazz.getName())) {
            return String.valueOf(val);
        }

        return QUOTE + JSON.toJSONString(val).replaceAll(QUOTE, "") + QUOTE;
    }

}
