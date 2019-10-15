package cn.laoshini.dk.register;

import java.util.List;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.dao.CsvFileDao;
import cn.laoshini.dk.dao.IDefaultDao;
import cn.laoshini.dk.dao.IEntityClassManager;
import cn.laoshini.dk.dao.JsonFileDao;
import cn.laoshini.dk.exception.BusinessException;

/**
 * 游戏数据加载器适配器，添加部分功能支持
 *
 * @author fagarine
 */
public abstract class AbstractGameDataLoaderAdaptor implements IGameDataLoader {

    private final String name;

    @FunctionDependent(nullable = true)
    private IDefaultDao defaultDao;

    @FunctionDependent(nullable = true)
    private IEntityClassManager entityClassManager;

    public AbstractGameDataLoaderAdaptor(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    protected final <T> List<T> readByDangKangDefaultDao(Class<T> entityClass) {
        if (defaultDao == null) {
            throw new BusinessException("default.dao.empty", "要使用当康系统自带的数据库DAO对象，IDefaultDao对象不能为空");
        }

        if (entityClassManager == null) {
            throw new BusinessException("entity.manager.empty", "要使用当康系统自带的数据库DAO对象，IEntityClassManager对象不能为空");
        }

        return defaultDao.selectAllEntity(entityClass);
    }

    /**
     * 从csv文件中读取数据
     *
     * @param filepath 文件路径
     * @param entityClass 单个对象类型
     * @param <T> 单个对象类型
     * @return 返回所有读取到的数据
     */
    protected <T> List<T> readFromCsvFile(String filepath, Class<T> entityClass) {
        return new CsvFileDao().readFromFile(filepath, entityClass);
    }

    /**
     * 从csv文件中读取数据
     * <p>
     * 注意：使用该方法，必须先保证实体类已经与文件名（不带后缀名）关联，并注册到{@link IEntityClassManager}的实现对象中，
     * 否则，将会抛出异常
     * </p>
     *
     * @param filepath 文件路径
     * @param <T> 单个对象类型
     * @return 返回所有读取到的数据
     */
    protected <T> List<T> readFromCsvFile(String filepath) {
        if (entityClassManager == null) {
            throw new BusinessException("entity.manager.empty", "IEntityClassManager对象不能为空");
        }

        return new CsvFileDao().readFromFile(filepath);
    }

    /**
     * 从JSON文件中读取数据
     *
     * @param filepath 文件路径
     * @param entityClass 单个对象类型
     * @param <T> 单个对象类型
     * @return 返回所有读取到的数据
     */
    protected <T> List<T> readFromJsonFile(String filepath, Class<T> entityClass) {
        return new JsonFileDao().readFromFile(filepath, entityClass);
    }

    /**
     * 从JSON文件中读取数据
     * <p>
     * 注意：使用该方法，必须先保证实体类已经与文件名（不带后缀名）关联，并注册到{@link IEntityClassManager}的实现对象中，
     * 否则，将会抛出异常
     * </p>
     *
     * @param filepath 文件路径
     * @param <T> 单个对象类型
     * @return 返回所有读取到的数据
     */
    protected <T> List<T> readFromJsonFile(String filepath) {
        if (entityClassManager == null) {
            throw new BusinessException("entity.manager.empty", "IEntityClassManager对象不能为空");
        }

        return new JsonFileDao().readFromFile(filepath);
    }
}
