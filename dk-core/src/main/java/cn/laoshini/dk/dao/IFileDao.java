package cn.laoshini.dk.dao;

import java.io.File;
import java.util.List;

import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.exception.DaoException;
import cn.laoshini.dk.function.VariousWaysManager;

/**
 * 文件数据访问对象接口
 *
 * @author fagarine
 */
public interface IFileDao {

    /**
     * 从文件中读取数据
     *
     * @param filePath 文件路径
     * @param entityType 单个对象类型
     * @param <E> 单个对象类型
     * @return 返回所有读取到的数据
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    <E> List<E> readFromFile(String filePath, Class<E> entityType) throws DaoException;

    /**
     * 从文件中读取数据（默认使用文件名关联查找对应的实体类型）
     * <p>
     * 注意：使用该方法，必须先保证实体类已经与文件名（不带后缀名）关联，并注册到{@link IEntityClassManager}的实现对象中，
     * 否则，将会抛出异常
     * </p>
     *
     * @param filePath 文件路径
     * @param <E> 返回数据的单个对象类型
     * @return 返回所有读取到的数据
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    default <E> List<E> readFromFile(String filePath) throws DaoException {
        return readFromFile(filePath, getClassByFileName(filePath));
    }

    /**
     * 把实体对象数据写入文件
     *
     * @param filePath 文件路径
     * @param entities 实体对象
     * @param <E> 实体对象类型
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    <E> void writeToFile(String filePath, List<E> entities) throws DaoException;

    /**
     * 从文件路径中获取文件对应的实体类
     * <p>
     * 注意：使用该方法，必须先保证实体类已经与文件名（不带后缀名）关联，并注册到{@link IEntityClassManager}的实现对象中，
     * 否则，将会抛出异常
     * </p>
     *
     * @param filePath 文件路径
     * @param <E> 返回数据的单个对象类型
     * @return 返回所有读取到的数据
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    default <E> Class<E> getClassByFileName(String filePath) throws DaoException {
        if (filePath == null) {
            throw new DaoException("dao.file.null", "文件路径不能为空");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new DaoException("dao.file.missing", "文件不存在:" + filePath);
        }
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf("."));

        try {
            IEntityClassManager entityClassManager = VariousWaysManager.getCurrentImpl(IEntityClassManager.class);
            Class<?> clazz = entityClassManager.getTableBeanClass(fileName);
            if (clazz == null) {
                throw new DaoException("dao.entity.missing", "找不到文件对应的实体类型:" + fileName);
            }
            return (Class<E>) clazz;
        } catch (BusinessException e) {
            throw new DaoException("entity.manager.empty", "IEntityClassManager实现对象不能为空");
        }
    }

}
