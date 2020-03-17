package cn.laoshini.dk.domain.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.laoshini.dk.util.StringUtil;

/**
 * 表单数据，对应excel文件的一个sheet，或一个数据库表等
 *
 * @param <RowType> 当行数据类型，可以是JavaBean，也可以使List，Map
 * @author fagarine
 */
public class ConstTable<RowType> {

    /**
     * 如果行类型是JavaBean，记录类
     */
    private Class<RowType> javabeanClass;

    /**
     * 表单名称
     */
    private String tableName;

    /**
     * 表头信息
     */
    private List<String> headers;

    /**
     * 所有行数据
     */
    private List<RowType> rows;

    public ConstTable() {
    }

    /**
     * 单行数据类型为JavaBean的构造方法
     *
     * @param name 表单名称
     * @param rows 所有行数据
     * @param javabeanClass JavaBean所属类
     */
    public ConstTable(String name, List<RowType> rows, Class<RowType> javabeanClass) {
        this.tableName = name;
        this.rows = rows;
        this.javabeanClass = javabeanClass;
        if (isJavaBean(javabeanClass)) {
            initJavaBeanHeader();
        }
    }

    /**
     * 单行数据类型为List&lt;Object&gt;的构造方法
     *
     * @param name 表单名称
     * @param headers 表头信息
     * @param rows 所有行数据
     */
    public ConstTable(String name, List<String> headers, List<RowType> rows) {
        this.tableName = name;
        this.headers = headers;
        this.rows = rows;
    }

    private void initJavaBeanHeader() {
        // 遍历JavaBean的参数名称
        Field[] fields = javabeanClass.getDeclaredFields();
        headers = new ArrayList<>(fields.length);
        for (Field field : fields) {
            headers.add(field.getName());
        }
    }

    /**
     * 将数据转换为带有完整信息的行格式（JavaBean或者Map&lt;name, value&gt;形式）并返回
     *
     * @return 该方法不会返回null
     */
    public List<?> toIntegratedList() {
        if (isEmpty()) {
            return Collections.emptyList();
        }

        if (isJavabean()) {
            return rows;
        }

        Class<?> rowClass = rows.get(0).getClass();
        if (!rowClass.isAssignableFrom(Map.class)) {
            return rows;
        }

        List<Map<String, Object>> list = new ArrayList<>(rows.size());
        for (RowType row : rows) {
            if (!rowClass.isAssignableFrom(List.class)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            List<Object> cells = (List<Object>) row;
            Map<String, Object> map = new HashMap<>(headers.size());
            for (int i = 0; i < headers.size(); i++) {
                map.put(headers.get(i), cells.get(i));
            }
            list.add(map);
        }
        return list;
    }

    public boolean isInvalid() {
        return StringUtil.isEmptyString(getTableName()) && isEmpty();
    }

    public boolean isEmpty() {
        return isEmpty(getHeaders()) && isEmptyRows();
    }

    public boolean isEmptyRows() {
        return isEmpty(getRows());
    }

    public int count() {
        if (isEmptyRows()) {
            return 0;
        }
        return getRows().size();
    }

    private boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public boolean isJavabean() {
        return isJavaBean(javabeanClass);
    }

    private boolean isJavaBean(Class<?> clazz) {
        return clazz != null && !clazz.isArray() && !clazz.isAssignableFrom(List.class) && !clazz
                .isAssignableFrom(Map.class);
    }

    public Class<RowType> getJavabeanClass() {
        return javabeanClass;
    }

    public void setJavabeanClass(Class<RowType> javabeanClass) {
        this.javabeanClass = javabeanClass;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<RowType> getRows() {
        return rows;
    }

    public void setRows(List<RowType> rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        return "ConstTable{" + "javabeanClass=" + javabeanClass + ", tableName='" + tableName + '\'' + ", headers="
               + headers + ", rows=" + rows + '}';
    }
}
