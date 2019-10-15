package cn.laoshini.dk.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.condition.ConditionalOnPropertyValue;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.dao.query.BeanQueryCondition;
import cn.laoshini.dk.dao.query.ListQueryCondition;
import cn.laoshini.dk.dao.query.Page;
import cn.laoshini.dk.dao.query.PageQueryCondition;
import cn.laoshini.dk.util.ReflectHelper;

/**
 * 项目内部已实现的DAO（项目实现并使用的）对象管理类，该类将作为公共DAO的统一操作入口
 * 更多信息参见{@link cn.laoshini.dk.dao}
 *
 * @author fagarine
 */
@Component
@FunctionVariousWays(description = "当康系统内建的默认DAO实现")
@ConditionalOnPropertyValue(propertyName = "dk.default-dao", havingValue = Constants.DEFAULT_PROPERTY_NAME)
public final class DefaultDaoImpl implements IDefaultDao {

    /**
     * 键值对数据库的数据访问对象（仅在系统不使用关系数据库启动的情况下存在）
     */
    @FunctionDependent(nullable = true, afterExecute = "init")
    private IPairDbDao pairDbDao;

    /**
     * 关系数据库数据访问对象（仅在系统使用关系数据库的情况下存在）
     */
    @FunctionDependent(nullable = true, afterExecute = "init")
    private IRelationalDbDaoManager relationalDbDaoManager;

    @FunctionDependent
    private IEntityClassManager entityClassManager;

    /**
     * 记录系统是否使用了关系数据库
     */
    private boolean useRdb;

    /**
     * 初始化系统内部缺省DAO
     */
    private void init() {
        try {
            SpringContextHolder.getBean("innerGameDataSource");
            useRdb = true;
        } catch (Exception e) {
            // 进入这里，说明找不到关系数据库配置，表示没有使用关系数据库
        }
    }

    @Override
    public boolean usePairDb() {
        return !useRdb;
    }

    @Override
    public boolean useRdb() {
        return useRdb;
    }

    public boolean isValid() {
        return pairDbDao != null || relationalDbDaoManager != null;
    }

    /**
     * 保存对象
     *
     * @param tableName 表名，用于关系数据库对象保存
     * @param key 对象对应的key，用于键值对数据库
     * @param bean 对象
     * @param <T> 对象类型
     */
    @Override
    public <T> void saveBean(String tableName, String key, T bean) {
        if (isValid() && bean != null) {
            if (usePairDb()) {
                pairDbDao.saveKeyValue(key, bean);
            } else if (useRdb()) {
                @SuppressWarnings("unchecked")
                Class<T> clazz = (Class<T>) bean.getClass();
                getDefaultRelationalDao(tableName, clazz).insert(bean);
            }
        }
    }

    /**
     * 保存实体类对象，仅用于被@{@link TableMapping}标记的类对象，且类中有变量被@{@link TableKey}标记
     *
     * @param bean 实体对象
     * @param <EntityType> 对象类型
     */
    @Override
    public <EntityType> void saveEntity(EntityType bean) {
        if (isValid() && bean != null) {
            @SuppressWarnings("unchecked")
            Class<EntityType> clazz = (Class<EntityType>) bean.getClass();
            if (entityClassManager.containsClass(clazz)) {
                String tableName = entityClassManager.getClassTableName(clazz.getName());
                if (usePairDb()) {
                    pairDbDao.saveKeyValue(ReflectHelper.getTableKey(tableName, bean), bean);
                } else if (useRdb()) {
                    getDefaultRelationalDao(tableName, clazz).insert(bean);
                }
            }
        }
    }

    /**
     * 保存多个对象到一个键下，仅用于被@{@link TableMapping}标记的类对象，且类中有变量被@{@link TableKey}标记
     *
     * @param key 保存的键名
     * @param beans 数据
     * @param <EntityType> 数据类型
     */
    @Override
    public <EntityType> void savePairEntityList(String key, List<EntityType> beans) {
        if (isValid() && beans != null && !beans.isEmpty()) {
            @SuppressWarnings("unchecked")
            Class<EntityType> clazz = (Class<EntityType>) beans.get(0).getClass();
            if (entityClassManager.containsClass(clazz)) {
                if (usePairDb()) {
                    pairDbDao.saveKeyValue(key, beans);
                }
            }
        }
    }

    /**
     * 保存多个对象到关系数据库中，仅用于被@{@link TableMapping}标记的类对象，且类中有变量被@{@link TableKey}标记
     *
     * @param beans 数据
     * @param <EntityType> 数据类型
     */
    @Override
    public <EntityType> void saveRelationalEntityList(List<EntityType> beans) {
        if (isValid() && beans != null && !beans.isEmpty()) {
            @SuppressWarnings("unchecked")
            Class<EntityType> clazz = (Class<EntityType>) beans.get(0).getClass();
            if (entityClassManager.containsClass(clazz)) {
                String tableName = entityClassManager.getClassTableName(clazz.getName());
                if (useRdb()) {
                    getDefaultRelationalDao(tableName, clazz).insertList(beans);
                }
            }
        }
    }

    /**
     * 查询单条数据
     *
     * @param tableName 表名称，关系数据库使用
     * @param clazz 对应的实体类
     * @param queryCondition 查询条件
     * @param <Type> 实体类型
     * @return 该方法可能返回null
     */
    @Override
    public <Type> Type selectBean(String tableName, Class<Type> clazz, BeanQueryCondition queryCondition) {
        if (!isValid()) {
            return null;
        }

        if (usePairDb()) {
            return pairDbDao.selectByCondition(queryCondition, clazz);
        } else if (useRdb()) {
            return getDefaultRelationalDao(tableName, clazz).selectByCondition(queryCondition);
        }
        return null;
    }

    /**
     * 查询单个实体对象，仅用于被@{@link TableMapping}标记的类对象
     *
     * @param clazz 实体类
     * @param queryCondition 查询条件
     * @param <EntityType> 实体类型
     * @return 该方法可能返回null
     */
    @Override
    public <EntityType> EntityType selectEntity(Class<EntityType> clazz, BeanQueryCondition queryCondition) {
        if (entityClassManager.containsClass(clazz)) {
            String tableName = entityClassManager.getClassTableName(clazz.getName());
            return selectBean(tableName, clazz, queryCondition);
        }
        return null;
    }

    /**
     * 查询多条数据
     *
     * @param tableName 表名称，关系数据库使用
     * @param clazz 对应的类
     * @param queryCondition 查询条件
     * @param <Type> 类型
     * @return 该方法可能返回null
     */
    @Override
    public <Type> List<Type> selectList(String tableName, Class<Type> clazz, ListQueryCondition queryCondition) {
        if (!isValid()) {
            return null;
        }

        if (usePairDb()) {
            return pairDbDao.selectListByCondition(queryCondition, clazz);
        } else if (useRdb()) {
            return getDefaultRelationalDao(tableName, clazz).selectListByCondition(queryCondition);
        }
        return null;
    }

    /**
     * 查询多个实体对象，仅用于被@{@link TableMapping}标记的类对象
     *
     * @param clazz 对应的实体类
     * @param queryCondition 查询条件
     * @param <EntityType> 实体类型
     * @return 该方法可能返回null
     */
    @Override
    public <EntityType> List<EntityType> selectEntityList(Class<EntityType> clazz, ListQueryCondition queryCondition) {
        if (entityClassManager.containsClass(clazz)) {
            String tableName = entityClassManager.getClassTableName(clazz.getName());
            return selectList(tableName, clazz, queryCondition);
        }
        return null;
    }

    @Override
    public <EntityType> List<EntityType> selectAllEntity(Class<EntityType> clazz) {
        if (entityClassManager.containsClass(clazz)) {
            String tableName = entityClassManager.getClassTableName(clazz.getName());
            return getDefaultRelationalDao(tableName, clazz).selectAll();
        }
        return null;
    }

    /**
     * 分页查询实体对象，仅用于被@{@link TableMapping}标记的类对象，仅用于关系数据库
     *
     * @param clazz 对应的实体类
     * @param pageCondition 查询条件
     * @param <EntityType> 实体类型
     * @return 该方法不会返回null
     */
    @Override
    public <EntityType> Page<EntityType> selectEntityByPage(Class<EntityType> clazz, PageQueryCondition pageCondition) {
        if (isValid() && useRdb() && entityClassManager.containsClass(clazz)) {
            String tableName = entityClassManager.getClassTableName(clazz.getName());
            return getDefaultRelationalDao(tableName, clazz).selectByPage(pageCondition);
        }
        return new Page<>(pageCondition.getPageNo(), pageCondition.getPageSize());
    }

    @Override
    public void deletePairByKey(String key) {
        if (isValid() && usePairDb()) {
            pairDbDao.deleteByKey(key);
        }
    }

    @Override
    public <EntityType> void deleteEntity(EntityType bean) {
        if (isValid() || bean == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Class<EntityType> clazz = (Class<EntityType>) bean.getClass();
        if (!entityClassManager.containsClass(clazz)) {
            return;
        }

        String tableName = entityClassManager.getClassTableName(clazz.getName());
        if (usePairDb()) {
            pairDbDao.deleteByKey(ReflectHelper.getTableKey(tableName, bean));
        } else if (useRdb()) {
            relationalDbDaoManager.getValidDbDao(tableName, clazz).delete(bean);
        }
    }

    private <EntityType> IRelationalDbDao<EntityType> getDefaultRelationalDao(String tableName,
            Class<EntityType> clazz) {
        if (isValid() && useRdb()) {
            return relationalDbDaoManager.getValidDbDao(tableName, clazz);
        }
        return null;
    }
}
