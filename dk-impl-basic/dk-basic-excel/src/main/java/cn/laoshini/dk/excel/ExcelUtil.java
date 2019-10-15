package cn.laoshini.dk.excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import cn.laoshini.dk.domain.common.ConstTable;
import cn.laoshini.dk.domain.common.MultiConstTableContent;
import cn.laoshini.dk.excel.constraint.ExcelHeader;
import cn.laoshini.dk.exception.BusinessException;

/**
 * excel数据读写工具类
 *
 * @author fagarine
 */
public class ExcelUtil {
    private ExcelUtil() {
    }

    public static final String EMPTY_MAP_JSON = JSON.toJSONString(Collections.emptyMap());

    /**
     * 读取并返回excel文件内容，单元格如果为空，将会使用null记录该单元格的值
     *
     * @param fileName 文件名称
     * @param input 数据流
     * @return 返回读取后的数据
     * @throws BusinessException 读取出错时，可能抛出异常
     */
    public static MultiConstTableContent readExcel(String fileName, InputStream input) throws BusinessException {
        return new ExcelReader(fileName, input).read();
    }

    /**
     * 读取并返回excel文件内容
     *
     * @param fileName 文件名称
     * @param input 数据流
     * @param nullable 是否允许单元格为空，如果不允许，遇到空单元格将会抛出异常
     * @return 读取出错时，可能抛出异常
     */
    public static MultiConstTableContent readExcel(String fileName, InputStream input, boolean nullable) {
        return new ExcelReader(fileName, input, nullable).read();
    }

    /**
     * 读取并返回excel文件内容
     *
     * @param fileName 文件名称
     * @param input 数据流
     * @param nullable 是否允许单元格为空，如果不允许，遇到空单元格将会抛出异常
     * @param defaultEmptyValue 该值仅在参数nullable为false，即允许单元格为空时有效，如果遇到空单元格，将使用该值记录
     * @return 读取出错时，可能抛出异常
     */
    public static MultiConstTableContent readExcel(String fileName, InputStream input, boolean nullable,
            Object defaultEmptyValue) {
        return new ExcelReader(fileName, input, nullable, defaultEmptyValue).read();
    }

    /**
     * 读取excel内容，并以实体类对象的集合的形式返回数据（适用于去读一个文件中多个表单，每个表单记录不同表数据的文件）
     * <p>
     * 注意：该方法将行数据转换为实体类的对象，依赖于{@link cn.laoshini.dk.dao.IEntityClassManager}，
     * 必须保证excel表单（sheet）的名称已与实体类映射，并已注册到IEntityClassManager对象中，否则将无法转换
     * </p>
     *
     * @param filePath 文件名称
     * @param hasHeader 表单中是否包含表头行
     * @param nullable 是否允许单元格为空，如果不允许，遇到空单元格将会抛出异常
     * @param defaultEmptyValue 该值仅在参数nullable为false，即允许单元格为空时有效，如果遇到空单元格，将使用该值记录
     * @return 返回读取到的数据，key为excel文件中表单的名称，value为在该表单中读取到的数据
     */
    public static Map<String, List<?>> readExcelAsEntity(String filePath, boolean hasHeader, boolean nullable,
            Object defaultEmptyValue) {
        try (InputStream in = new FileInputStream(filePath)) {
            ExcelReader reader = ExcelReader.entityReader(filePath, in, hasHeader, nullable, defaultEmptyValue);
            reader.setTransferToBean(true);
            MultiConstTableContent content = reader.read();
            if (content.isEmpty()) {
                return Collections.emptyMap();
            }

            Map<String, List<?>> tableMap = new HashMap<>(content.getTables().size());
            for (ConstTable<?> table : content.getTables()) {
                if (table.isInvalid()) {
                    continue;
                }

                tableMap.put(table.getTableName(), table.getRows());
            }
            return tableMap;
        } catch (IOException e) {
            throw new BusinessException("read.excel.error", "excel文件读取出错:" + filePath, e);
        }
    }

    /**
     * 读取excel内容，并以JavaBean集合的形式返回数据（适用于读取单个表单的文件）
     *
     * @param filePath 文件名称
     * @param hasHeader 表单中是否包含表头行
     * @param nullable 是否允许单元格为空，如果不允许，遇到空单元格将会抛出异常
     * @param defaultEmptyValue 该值仅在参数nullable为false，即允许单元格为空时有效，如果遇到空单元格，将使用该值记录
     * @param beanClass JavaBean类型
     * @param <E> JavaBean类型
     * @return 返回读取到的数据，如果文件中存在多个表单，且都有数据，则会将多个表单的数据合并后返回
     */
    @SuppressWarnings("unchecked")
    public static <E> List<E> readExcelAsJavaBean(String filePath, boolean hasHeader, boolean nullable,
            Object defaultEmptyValue, Class<E> beanClass) {
        try (InputStream in = new FileInputStream(filePath)) {
            ExcelReader reader = ExcelReader
                    .javaBeanReader(filePath, in, hasHeader, nullable, defaultEmptyValue, beanClass);

            MultiConstTableContent content = reader.read();
            if (content.isEmpty()) {
                return Collections.emptyList();
            }

            List<E> beans = new ArrayList<>(content.totalRowCount());
            for (ConstTable<?> table : content.getTables()) {
                if (!table.isEmptyRows()) {
                    beans.addAll((Collection<? extends E>) table.getRows());
                }
            }
            return beans;
        } catch (IOException e) {
            throw new BusinessException("read.excel.error", "excel文件读取出错:" + filePath, e);
        }
    }

    /**
     * 将excel中的数据转换为JSON字符串返回
     *
     * @param fileName 文件名称
     * @param input 数据流
     * @param nullable 是否允许单元格为空，如果不允许，遇到空单元格将会抛出异常
     * @param defaultEmptyValue 该值仅在参数nullable为false，即允许单元格为空时有效，如果遇到空单元格，将使用该值记录
     * @return 读取出错时，可能抛出异常
     */
    public static String readExcelWithJson(String fileName, InputStream input, boolean nullable,
            Object defaultEmptyValue) {
        MultiConstTableContent content = readExcel(fileName, input, nullable, defaultEmptyValue);
        if (content.isEmpty()) {
            return EMPTY_MAP_JSON;
        }

        Map<String, List<?>> tableMap = new HashMap<>(content.getTables().size());
        for (ConstTable<?> table : content.getTables()) {
            if (table.isInvalid()) {
                continue;
            }

            tableMap.put(table.getTableName(), table.toIntegratedList());
        }
        return JSON.toJSONString(tableMap);
    }

    /**
     * 导出excel数据到输出流中
     *
     * @param output 输出流
     * @param content excel数据内容
     */
    public static void exportExcel(OutputStream output, MultiConstTableContent content) {
        if (output == null || content == null) {
            return;
        }
        new ExcelExporter(content.getTables(), output).export();
    }

    /**
     * 导出excel数据到输出流中
     *
     * @param output 输出流
     * @param tableMap 表单数据Map，key为表单名称，value为表单数据
     * @param <RowType> 单行数据类型，可以是JavaBean，List，数组
     */
    public static <RowType> void exportExcel(OutputStream output, Map<String, List<RowType>> tableMap) {
        if (output == null || tableMap == null) {
            return;
        }

        List<ConstTable<?>> excelTables = new ArrayList<>(tableMap.size());
        for (Map.Entry<String, List<RowType>> entry : tableMap.entrySet()) {
            List<RowType> rows = entry.getValue();
            if (rows == null || rows.isEmpty()) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<RowType> rowTypeClass = (Class<RowType>) rows.get(0).getClass();
            if (rowTypeClass.isArray() || List.class.equals(rowTypeClass)) {
                // 数组和集合形式的数据，没有表头
                excelTables.add(new ConstTable<>(entry.getKey(), null, entry.getValue()));
            } else {
                // JavaBean形式的数据，表头使用JavaBean类中的参数名称
                excelTables.add(new ConstTable<>(entry.getKey(), entry.getValue(), rowTypeClass));
            }
        }
        new ExcelExporter(excelTables, output).export();
    }

    /**
     * 导出单个excel表单数据到输出流中
     *
     * @param output 输出流
     * @param table 单个表单的数据
     * @param <RowType> 单行数据类型，可以是JavaBean，List，数组
     */
    public static <RowType> void exportSingleExcelTable(OutputStream output, ConstTable<RowType> table) {
        if (output == null || table == null || table.isInvalid()) {
            return;
        }
        new ExcelExporter(table, output).export();
    }

    /**
     * 导出单个excel表单数据到输出流中
     *
     * @param output 输出流
     * @param rows 所有行数据
     * @param headers 表头信息
     * @param sheetName 表单名称
     * @param <RowType> 单行数据类型，可以是JavaBean，List，数组
     */
    public static <RowType> void exportSingleExcelSheet(OutputStream output, List<RowType> rows, List<String> headers,
            String sheetName) {
        new ExcelExporter(new ConstTable<>(sheetName, headers, rows), output).export();
    }

    /**
     * 将excel数据写入到输出流
     *
     * @param output 输出流
     * @param rows 数据内容
     * @param headers 表头内容，支持扩展功能
     * @param sheetTitle 表单名称，标题
     * @param <RowType> 单行数据对象类型，可以是JavaBean、List、数组
     */
    public static <RowType> void exportExcel(OutputStream output, List<RowType> rows, List<ExcelHeader> headers,
            String sheetTitle) {
        new ConstraintExcelExporter<>(output, sheetTitle, headers, rows).export();
    }

    /**
     * 将excel数据写入到输出流
     *
     * @param output 输出流
     * @param rows 数据内容
     * @param headers 表头内容，支持扩展功能
     * @param sheetTitle 表单名称，标题
     * @param protection 表单是否受保护，如果为true，则只有指定可编辑的单元格才可以编辑，默认为false
     * @param <RowType> 单行数据对象类型，可以是JavaBean、List、数组
     */
    public static <RowType> void exportExcel(OutputStream output, List<RowType> rows, List<ExcelHeader> headers,
            String sheetTitle, boolean protection) {
        new ConstraintExcelExporter<>(output, sheetTitle, headers, rows, true, protection).export();
    }

    /**
     * 将excel数据写入到输出流
     *
     * @param output 输出流
     * @param rows 数据内容
     * @param headers 表头内容，支持扩展功能
     * @param sheetTitle 表单名称，标题
     * @param fillValueByType 是否按照数据类型填充单元格，默认为true，如果为false，数据将以字符串形式写入表格
     * @param protection 表单是否受保护，如果为true，则只有指定可编辑的单元格才可以编辑，默认为false
     * @param <RowType> 单行数据对象类型，可以是JavaBean、List、数组
     */
    public static <RowType> void exportExcel(OutputStream output, List<RowType> rows, List<ExcelHeader> headers,
            String sheetTitle, boolean fillValueByType, boolean protection) {
        new ConstraintExcelExporter<>(output, sheetTitle, headers, rows, fillValueByType, protection).export();
    }

    /**
     * 将excel数据以字符串形式写入输出流
     *
     * @param output 输出流
     * @param rows 数据内容
     * @param headers 表头内容，支持扩展功能
     * @param sheetTitle 表单名称，标题
     * @param <RowType> 单行数据对象类型，可以是JavaBean、List、数组
     */
    public static <RowType> void exportStringDataToExcel(OutputStream output, List<RowType> rows,
            List<ExcelHeader> headers, String sheetTitle) {
        new ConstraintExcelExporter<>(output, sheetTitle, headers, rows, false).export();
    }
}
