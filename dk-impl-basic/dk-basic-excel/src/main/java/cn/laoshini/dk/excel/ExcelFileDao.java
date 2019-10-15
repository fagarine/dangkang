package cn.laoshini.dk.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cn.laoshini.dk.dao.IEntityClassManager;
import cn.laoshini.dk.dao.IFileDao;
import cn.laoshini.dk.excel.constraint.ExcelHeader;
import cn.laoshini.dk.exception.DaoException;
import cn.laoshini.dk.function.VariousWaysManager;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.FileUtil;

/**
 * excel文件访问对象
 *
 * @author fagarine
 */
public class ExcelFileDao implements IFileDao {

    private boolean hasHeader;

    public ExcelFileDao(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    @Override
    public <E> List<E> readFromFile(String filePath, Class<E> entityType) throws DaoException {
        File file = checkFilePath(filePath);

        if (entityType == null) {
            throw new DaoException("dao.entity.null", "实体类型不能为空");
        }

        return readFileContent(file.getAbsolutePath(), entityType);
    }

    private <E> List<E> readFileContent(String filePath, Class<E> entityType) {
        return ExcelUtil.readExcelAsJavaBean(filePath, hasHeader, true, "", entityType);
    }

    private File checkFilePath(String filePath) {
        if (filePath == null) {
            throw new DaoException("dao.file.null", "文件路径不能为空");
        }

        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            throw new DaoException("dao.file.missing", "文件不存在:" + filePath);
        }

        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
            throw new DaoException("dao.file.error", "文件后缀名不匹配:" + filePath);
        }
        return file;
    }

    @Override
    public <E> void writeToFile(String filePath, List<E> entities) throws DaoException {
        if (CollectionUtil.isEmpty(entities)) {
            return;
        }

        Class<?> type = entities.get(0).getClass();
        IEntityClassManager entityClassManager = VariousWaysManager.getCurrentImpl(IEntityClassManager.class);
        if (entityClassManager == null) {
            throw new DaoException("dao.file.entity", "IEntityClassManager为空，无法识别类的导出名称:" + type.getName());
        }

        String sheetName = entityClassManager.getClassTableName(type.getName());
        if (sheetName == null) {
            throw new DaoException("dao.file.class", "找不到别类的导出名称:" + type.getName());
        }

        writeToFile(filePath, sheetName, entities);
    }

    public <E> void writeToFile(String filePath, String sheetName, List<E> entities) throws DaoException {
        if (CollectionUtil.isEmpty(entities)) {
            return;
        }

        File file = FileUtil.createFile(filePath);
        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
            throw new DaoException("dao.file.error", "文件后缀名不匹配:" + filePath);
        }

        Field[] fields = entities.get(0).getClass().getDeclaredFields();
        List<ExcelHeader> headers = new ArrayList<>(fields.length);
        for (Field field : fields) {
            headers.add(ExcelHeader.newHeader(field.getName()));
        }

        try (OutputStream os = new FileOutputStream(file)) {
            ExcelUtil.exportExcel(os, entities, headers, sheetName);
        } catch (IOException e) {
            throw new DaoException("dao.file.error", "写入excel文件出错:" + filePath, e);
        }
    }
}
