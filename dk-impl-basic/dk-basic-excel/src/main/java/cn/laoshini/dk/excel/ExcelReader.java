package cn.laoshini.dk.excel;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.dao.IEntityClassManager;
import cn.laoshini.dk.domain.common.ConstReadTable;
import cn.laoshini.dk.domain.common.MultiConstTableContent;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.StringUtil;
import cn.laoshini.dk.util.TypeHelper;

/**
 * excel数据读取类
 *
 * @author fagarine
 */
class ExcelReader {

    @FunctionDependent
    private IEntityClassManager entityClassManager;

    /**
     * 2003- 版本的excel
     */
    private final static String EXCEL_2003_L = ".xls";
    /**
     * 2007+ 版本的excel
     */
    private final static String EXCEL_2007_U = ".xlsx";

    /**
     * excel文件名称
     */
    private String fileName;

    /**
     * excel文件输入流
     */
    private InputStream in;

    /**
     * 表单中是否包含表头行，默认为true
     */
    private boolean hasHeader = true;

    /**
     * 是否允许单元格出现空，如果不允许，当发现空单元格时，会中断操作，抛出异常，默认为true
     */
    private boolean nullable = true;

    /**
     * 当允许出现空单元格时，使用该值记录空单元格的值，默认为null
     */
    private Object defaultEmptyValue;

    /**
     * 是否转换为JavaBean
     */
    private boolean transferToBean;

    /**
     * 如果要转换为指定的对象类型，记录JavaBean类型，仅在transferToBean为true时有效
     */
    private Class<?> beanClass;

    /**
     * 单元格公式计算器
     */
    private FormulaEvaluator evaluator;

    public ExcelReader(String fileName, InputStream in) {
        this.fileName = fileName;
        this.in = in;
    }

    public ExcelReader(String fileName, InputStream in, boolean nullable) {
        this.fileName = fileName;
        this.in = in;
        this.nullable = nullable;
    }

    public ExcelReader(String fileName, InputStream in, boolean nullable, Object defaultEmptyValue) {
        this.fileName = fileName;
        this.in = in;
        this.nullable = nullable;
        this.defaultEmptyValue = defaultEmptyValue;
    }

    public static ExcelReader entityReader(String fileName, InputStream in, boolean hasHeader, boolean nullable,
            Object defaultEmptyValue) {
        ExcelReader reader = new ExcelReader(fileName, in, nullable, defaultEmptyValue);
        reader.setTransferToBean(true);
        reader.setHasHeader(hasHeader);
        return reader;
    }

    public static ExcelReader javaBeanReader(String fileName, InputStream in, boolean hasHeader, boolean nullable,
            Object defaultEmptyValue, Class<?> beanClass) {
        ExcelReader reader = new ExcelReader(fileName, in, nullable, defaultEmptyValue);
        reader.setTransferToBean(true);
        reader.setBeanClass(beanClass);
        reader.setHasHeader(hasHeader);
        return reader;
    }

    /**
     * 读取并返回excel文件内容
     *
     * @return 该方法不会返回null
     * @throws BusinessException 读取出错时，可能抛出异常
     */
    public MultiConstTableContent read() throws BusinessException {
        // 创建Excel工作薄
        try (Workbook work = createWorkbook()) {
            return readWorkbook(work);
        } catch (IOException e) {
            throw new BusinessException("read.excel.fail", "创建Excel工作簿失败, 请检查文件:" + fileName);
        }

    }

    /**
     * 读取并返回工作簿内容
     *
     * @param work 工作簿对象
     * @return 该方法不会返回null
     */
    private MultiConstTableContent readWorkbook(Workbook work) {
        MultiConstTableContent excelContent = new MultiConstTableContent(fileName);
        evaluator = work.getCreationHelper().createFormulaEvaluator();

        Sheet sheet;
        Class<?> rowType;
        // 遍历Excel中所有的sheet
        for (int i = 0; i < work.getNumberOfSheets(); i++) {
            sheet = work.getSheetAt(i);
            if (sheet == null) {
                continue;
            }

            String sheetName = sheet.getSheetName();
            if (transferToBean) {
                if (beanClass != null) {
                    rowType = beanClass;
                } else {
                    rowType = entityClassManager.getTableBeanClass(sheetName);
                    if (rowType == null) {
                        throw new BusinessException("excel.read.error",
                                String.format("找不到sheet对应的实体类, file:%s, sheet:%s", fileName, sheetName));
                    }
                }
            } else {
                rowType = List.class;
            }
            excelContent.addTable(readSheet(sheet, rowType));
        }
        return excelContent;
    }

    /**
     * 读个单个表单
     *
     * @param sheet 表单对象
     * @param rowTypeClass 当行数据记录类型
     * @param <RowType> 当行数据记录类型
     * @return 返回整个表单的数据
     */
    private <RowType> ConstReadTable<RowType> readSheet(Sheet sheet, Class<RowType> rowTypeClass) {
        if (sheet.getLastRowNum() <= 0) {
            return null;
        }

        int totalRow = sheet.getLastRowNum() - sheet.getFirstRowNum() + 1;
        List<String> headers = new ArrayList<>();
        List<RowType> rows = new ArrayList<>(totalRow);

        // 读取数据，并记录读取过程中出现的错误信息
        String errorMsg = readSheetToExcelTable(sheet, headers, rows, totalRow, rowTypeClass);
        ConstReadTable<RowType> table = new ConstReadTable<>();
        table.setRows(rows);
        table.setHeaders(headers);
        table.setErrorMsg(errorMsg);
        table.setJavabeanClass(rowTypeClass);
        table.setTableName(sheet.getSheetName());
        table.setFaulty(StringUtil.isEmptyString(errorMsg));
        return table;
    }

    /**
     * 读取表单数据，并填充
     *
     * @param sheet 表单对象
     * @param headers 表头信息
     * @param rows 所有行数据
     * @param totalRow 总行数
     * @param rowTypeClass 当行数据记录类型
     * @param <RowType> 当行数据记录类型
     * @return 返回读取出错的记录
     */
    private <RowType> String readSheetToExcelTable(Sheet sheet, List<String> headers, List<RowType> rows, int totalRow,
            Class<RowType> rowTypeClass) {
        // 记录错误信息
        StringBuilder errorMsg = new StringBuilder();
        Field[] fields = null;
        List<RowType> rowList = null;
        Map<String, Object> cellMap = null;
        List<Map<String, Object>> cellMapList = null;
        // 记录行类型是否是JavaBean，如果不是则将行数据记录为List<Object>
        boolean notBean = List.class.equals(rowTypeClass);
        if (notBean) {
            rowList = new ArrayList<>(totalRow);
        } else {
            fields = rowTypeClass.getDeclaredFields();
            cellMapList = new ArrayList<>(totalRow);
        }

        // 遍历当前sheet中的所有行
        String sheetName = sheet.getSheetName();
        for (int rowNum = sheet.getFirstRowNum(); rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                continue;
            }

            // 是否是表头行
            boolean isHeader = hasHeader && rowNum == sheet.getFirstRowNum();
            if (!isHeader) {
                cellMap = new LinkedHashMap<>(headers.size());
            }

            // 遍历所有的列
            for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
                Cell cell = row.getCell(cellNum);
                int index = cellNum - row.getFirstCellNum();
                if (null == cell || cell.getCellType() == CellType.BLANK) {
                    if (!nullable || isHeader) {
                        throw new BusinessException("excel.cell.null",
                                String.format("文件[%s]表单[%s]第[%d]行第[%d]列出现空数据", fileName, sheetName, rowNum + 1,
                                        cellNum + 1));
                    }
                    errorMsg.append(String.format("第[%d]行第[%d]列出现空数据；", rowNum + 1, cellNum + 1))
                            .append(Constants.LINE_SEPARATOR);
                    cellMap.put(headers.get(index), getDefaultEmptyValue());
                    continue;
                }

                try {
                    if (isHeader) {
                        headers.add(String.valueOf(getCellValue(cell)));
                    } else {
                        if (hasHeader) {
                            cellMap.put(headers.get(index), getCellValue(cell));
                        } else if (notBean) {
                            cellMap.put(String.valueOf(index), getCellValue(cell));
                        } else {
                            if (index >= fields.length) {
                                break;
                            }

                            cellMap.put(fields[index].getName(), getCellValue(cell));
                        }
                    }
                } catch (Exception e) {
                    if (!nullable) {
                        throw new BusinessException("excel.cell.error",
                                String.format("文件[%s]表单[%s]第[%d]行第[%d]列读取出错", fileName, sheetName, rowNum + 1,
                                        cellNum + 1), e);
                    }
                    errorMsg.append(String.format("第[%d]行第[%d]列读取出错；", rowNum + 1, cellNum + 1))
                            .append(Constants.LINE_SEPARATOR);
                }
            }

            if (!isHeader) {
                if (notBean) {
                    //noinspection unchecked
                    rowList.add((RowType) new ArrayList<>(cellMap.values()));
                } else {
                    cellMapList.add(cellMap);
                }
            }
        }

        // 如果不需要讲行数据转换为JavaBean，则以List<Object>形式返回行数据，否则转换行数据
        if (notBean) {
            rows.addAll(rowList);
        } else {
            rows.addAll(TypeHelper.mapToBeanList(cellMapList, rowTypeClass));
        }
        return errorMsg.toString();
    }

    /**
     * 对表格中数值进行格式化并以字符串形式返回
     *
     * @param cell 单元格对象
     * @return 返回单元格的值
     */
    private Object getCellValue(Cell cell) {
        Object value;
        try {
            switch (cell.getCellType()) {
                case STRING:
                    value = cell.getStringCellValue();
                    break;
                case NUMERIC:
                    value = parseNumeric(cell);
                    break;
                case BOOLEAN:
                    value = cell.getBooleanCellValue();
                    break;
                case FORMULA:
                    // 先计算单元格的值
                    evaluator.evaluateFormulaCell(cell);
                    try {
                        // 由于不知道公式计算后为字符串还是数值，先尝试字符串，如果出错，则表示结果为数值
                        value = cell.getStringCellValue();
                    } catch (Exception e) {
                        value = cell.getNumericCellValue();
                    }
                    break;
                case BLANK:
                default:
                    value = getDefaultEmptyValue();
                    break;
            }
        } catch (Exception e) {
            LogUtil.error(String.format("单元格读取出错, file:%s, cell:%s", fileName, cell), e);
            value = getDefaultEmptyValue();
        }
        return value;
    }

    private Object parseNumeric(Cell cell) {
        if (CellType.NUMERIC != cell.getCellType()) {
            return getDefaultEmptyValue();
        }

        Object value;
        if (HSSFDateUtil.isCellDateFormatted(cell)) {
            value = cell.getDateCellValue();
        } else if (cell.getCellStyle().getDataFormat() == 0x3a) {
            value = DateUtil.getJavaDate(cell.getNumericCellValue());
        } else {
            BigDecimal num = BigDecimal.valueOf(cell.getNumericCellValue());
            if (num.scale() > 0) {
                value = num.doubleValue();
            } else {
                value = num.longValue();
            }
        }
        return value;
    }

    private Workbook createWorkbook() throws IOException {
        Workbook wb;
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if (EXCEL_2003_L.equalsIgnoreCase(fileType)) {
            // 2003-
            wb = new HSSFWorkbook(in);
        } else if (EXCEL_2007_U.equalsIgnoreCase(fileType)) {
            // 2007+
            wb = new XSSFWorkbook(in);
        } else {
            throw new BusinessException("read.excel.unsupported", "文件解析失败,请下载正确模板");
        }
        return wb;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InputStream getIn() {
        return in;
    }

    public void setIn(InputStream in) {
        this.in = in;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    private Object getDefaultEmptyValue() {
        return defaultEmptyValue;
    }

    public void setDefaultEmptyValue(Object defaultEmptyValue) {
        this.defaultEmptyValue = defaultEmptyValue;
    }

    public boolean isTransferToBean() {
        return transferToBean;
    }

    public void setTransferToBean(boolean transferToBean) {
        this.transferToBean = transferToBean;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }
}
