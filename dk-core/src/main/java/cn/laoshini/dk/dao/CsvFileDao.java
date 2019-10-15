package cn.laoshini.dk.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.exception.DaoException;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.FileUtil;
import cn.laoshini.dk.util.ReflectUtil;
import cn.laoshini.dk.util.StringUtil;
import cn.laoshini.dk.util.TypeHelper;

/**
 * CSV文件访问对象实现
 *
 * @author fagarine
 */
public class CsvFileDao implements IFileDao {

    private static final String SUFFIX = ".csv";

    private static final String SEPARATOR = Constants.SEPARATOR_COMMA;

    @Override
    public <E> List<E> readFromFile(String filePath, Class<E> entityType) throws DaoException {
        File file = checkFilePath(filePath);

        if (entityType == null) {
            throw new DaoException("dao.entity.null", "实体类型不能为空");
        }

        return readEntities(file, entityType);
    }

    private <E> List<E> readEntities(File file, Class<E> entityType) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return readFileContent(reader, entityType);
        } catch (IOException e) {
            throw new DaoException("file.dao.error", "实体数据读取出错:" + file.getPath(), e);
        }
    }

    private <E> List<E> readFileContent(BufferedReader reader, Class<E> entityType) throws IOException {
        String line;
        String[] params;
        Map<String, Object> rowMap;
        List<Map<String, Object>> paramMapList = new LinkedList<>();
        Field[] fields = entityType.getDeclaredFields();
        while ((line = reader.readLine()) != null) {
            params = line.split(SEPARATOR);
            if (params.length > 0) {
                rowMap = new HashMap<>(fields.length);
                paramMapList.add(rowMap);
                for (int i = 0; i < params.length && i < fields.length; i++) {
                    String param = params[i];
                    rowMap.put(fields[i].getName(), param.trim());
                }
            }
        }

        return TypeHelper.mapToBeanList(paramMapList, entityType);
    }

    private File checkFilePath(String filePath) {
        if (StringUtil.isEmptyString(filePath)) {
            throw new DaoException("dao.file.null", "文件路径不能为空");
        }

        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            throw new DaoException("dao.file.missing", "文件不存在:" + filePath);
        }

        if (!filePath.toLowerCase().endsWith(SUFFIX)) {
            throw new DaoException("dao.file.error", "文件后缀名不匹配");
        }
        return file;
    }

    @Override
    public <E> void writeToFile(String filePath, List<E> entities) throws DaoException {
        if (CollectionUtil.isEmpty(entities)) {
            return;
        }

        File file = FileUtil.createFile(filePath);

        if (!filePath.toLowerCase().endsWith(SUFFIX)) {
            throw new DaoException("dao.file.error", "文件后缀名不匹配");
        }

        Field[] fields = null;
        StringBuilder content = new StringBuilder(entities.size() * 64);
        for (E entity : entities) {
            if (entity == null) {
                continue;
            }

            int index = 0;
            if (fields == null) {
                fields = entity.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (index++ > 0) {
                        content.append(Constants.SEPARATOR_COMMA);
                    }
                    content.append(field.getName());
                }
                content.append(Constants.LINE_SEPARATOR);
            }

            index = 0;
            for (Field field : fields) {
                if (index++ > 0) {
                    content.append(Constants.SEPARATOR_COMMA);
                }
                content.append(ReflectUtil.getFieldValue(entity, field));
            }
            content.append(Constants.LINE_SEPARATOR);
        }

        FileUtil.writeFile(file, content.toString());
    }
}
