package cn.laoshini.dk.domain.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 多个常量表单（数据配置文件或数据库表）数据表示类
 * 如表示一个包含多个sheet的excel文件内容
 *
 * @author fagarine
 */
public class MultiConstTableContent {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * excel文件中的所有表单信息，一个ConstTable对应一个sheet
     */
    private List<ConstTable<?>> tables;

    public MultiConstTableContent() {
    }

    public MultiConstTableContent(String fileName) {
        this.fileName = fileName;
    }

    public MultiConstTableContent(String fileName, List<ConstTable<?>> tables) {
        this.fileName = fileName;
        this.tables = tables;
    }

    /**
     * 添加新的表单数据
     *
     * @param table 单个表单数据
     */
    public void addTable(ConstTable table) {
        if (tables == null) {
            tables = new ArrayList<>();
        }
        tables.add(table);
    }

    /**
     * 数据是否为空
     *
     * @return 返回数据是否为空
     */
    public boolean isEmpty() {
        return tables == null || tables.isEmpty();
    }

    /**
     * 获取表单数据总行数
     *
     * @return 返回非负整数
     */
    public int totalRowCount() {
        if (isEmpty()) {
            return 0;
        }

        int count = 0;
        for (ConstTable<?> table : tables) {
            count += table.count();
        }
        return count;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<ConstTable<?>> getTables() {
        return tables;
    }

    public void setTables(List<ConstTable<?>> tables) {
        this.tables = tables;
    }

    @Override
    public String toString() {
        return "MultiConstTableContent{" + "fileName='" + fileName + '\'' + ", tables=" + tables + '}';
    }
}
