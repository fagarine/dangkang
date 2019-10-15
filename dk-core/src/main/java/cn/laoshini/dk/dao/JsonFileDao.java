package cn.laoshini.dk.dao;

import java.io.File;
import java.util.List;

import cn.laoshini.dk.exception.DaoException;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.JsonUtil;

/**
 * JSON文件访问对象实现
 *
 * @author fagarine
 */
public class JsonFileDao implements IFileDao {

    @Override
    public <E> List<E> readFromFile(String filePath, Class<E> entityType) throws DaoException {
        File file = checkFilePath(filePath);

        return JsonUtil.readBeanList(file.getAbsolutePath(), entityType);
    }

    private File checkFilePath(String filePath) {
        if (filePath == null) {
            throw new DaoException("dao.file.null", "文件路径不能为空");
        }

        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            throw new DaoException("dao.file.missing", "文件不存在:" + filePath);
        }

        return file;
    }

    @Override
    public <E> void writeToFile(String filePath, List<E> entities) throws DaoException {
        if (CollectionUtil.isEmpty(entities)) {
            return;
        }

        JsonUtil.writeJsonFile(filePath, entities);
    }
}
