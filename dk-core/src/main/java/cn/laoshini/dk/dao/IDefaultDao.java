package cn.laoshini.dk.dao;

import java.util.List;

import cn.laoshini.dk.annotation.ConfigurableFunction;
import cn.laoshini.dk.dao.query.BeanQueryCondition;
import cn.laoshini.dk.dao.query.ListQueryCondition;
import cn.laoshini.dk.dao.query.Page;
import cn.laoshini.dk.dao.query.PageQueryCondition;

/**
 * 当康系统提供的缺省DAO接口，提供统一的数据访问接口
 *
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.default-dao", description = "系统默认DAO")
public interface IDefaultDao {

    /**
     * 当前是否使用的是关系型数据库
     *
     * @return 返回判断结果
     */
    boolean useRdb();

    /**
     * 当前是否使用的是键值对数据库
     *
     * @return 返回判断结果
     */
    boolean usePairDb();

    /**
     * 保存对象（关系数据库和键值对数据库通用）
     *
     * @param tableName 表名，用于关系数据库对象保存
     * @param key 对象对应的key，用于键值对数据库
     * @param bean 对象
     * @param <T> 对象类型
     */
    <T> void saveBean(String tableName, String key, T bean);

    /**
     * 保存实体类对象，仅用于被@{@link TableMapping}标记的类对象，且类中有变量被@{@link TableKey}标记（关系型数据库使用）
     *
     * @param bean 实体对象
     * @param <EntityType> 对象类型
     */
    <EntityType> void saveEntity(EntityType bean);

    /**
     * 保存多个对象到一个键下，仅用于被@{@link TableMapping}标记的类对象，且类中有变量被@{@link TableKey}标记（键值对数据库使用）
     *
     * @param key 保存的键名
     * @param beans 数据
     * @param <EntityType> 数据类型
     */
    <EntityType> void savePairEntityList(String key, List<EntityType> beans);

    /**
     * 保存多个对象到关系数据库中，仅用于被@{@link TableMapping}标记的类对象，且类中有变量被@{@link TableKey}标记（关系型数据库使用）
     *
     * @param beans 数据
     * @param <EntityType> 数据类型
     */
    <EntityType> void saveRelationalEntityList(List<EntityType> beans);

    /**
     * 更新数据
     *
     * @param bean 数据对象
     * @param <EntityType> 数据类型
     */
    <EntityType> void updateEntity(EntityType bean);

    /**
     * 更新多条数据（仅用于关系型数据库）
     *
     * @param beans 数据对象集合
     * @param <EntityType> 数据类型
     */
    <EntityType> void updateRelationalEntityList(List<EntityType> beans);

    /**
     * 查询单条数据（关系数据库和键值对数据库通用）
     *
     * @param tableName 表名称，关系数据库使用
     * @param clazz 对应的实体类
     * @param queryCondition 查询条件
     * @param <Type> 实体类型
     * @return 该方法可能返回null
     */
    <Type> Type selectBean(String tableName, Class<Type> clazz, BeanQueryCondition queryCondition);

    /**
     * 查询单个实体对象，仅用于被@{@link TableMapping}标记的类对象（关系型数据库使用）
     *
     * @param clazz 实体类
     * @param queryCondition 查询条件
     * @param <EntityType> 实体类型
     * @return 该方法可能返回null
     */
    <EntityType> EntityType selectEntity(Class<EntityType> clazz, BeanQueryCondition queryCondition);

    /**
     * 查询多条数据（关系数据库和键值对数据库通用）
     *
     * @param tableName 表名称，关系数据库使用
     * @param clazz 对应的类
     * @param queryCondition 查询条件
     * @param <Type> 类型
     * @return 该方法可能返回null
     */
    <Type> List<Type> selectList(String tableName, Class<Type> clazz, ListQueryCondition queryCondition);

    /**
     * 查询多个实体对象，仅用于被@{@link TableMapping}标记的类对象（关系型数据库使用）
     *
     * @param clazz 对应的实体类
     * @param queryCondition 查询条件
     * @param <EntityType> 实体类型
     * @return 该方法可能返回null
     */
    <EntityType> List<EntityType> selectEntityList(Class<EntityType> clazz, ListQueryCondition queryCondition);

    /**
     * 查询表用所有数据（关系型数据库使用）
     *
     * @param clazz 对应的实体类
     * @param <EntityType> 实体类型
     * @return 该方法可能返回null
     */
    <EntityType> List<EntityType> selectAllEntity(Class<EntityType> clazz);

    /**
     * 分页查询实体对象，仅用于被@{@link TableMapping}标记的类对象，仅用于关系数据库
     *
     * @param clazz 对应的实体类
     * @param pageCondition 查询条件
     * @param <EntityType> 实体类型
     * @return 该方法不会返回null
     */
    <EntityType> Page<EntityType> selectEntityByPage(Class<EntityType> clazz, PageQueryCondition pageCondition);

    /**
     * 删除键值对数据库中指定键的数据
     *
     * @param key key
     */
    void deletePairByKey(String key);

    /**
     * 删除指定实体对象，仅用于被@{@link TableMapping}标记的类对象（关系数据库和键值对数据库通用）
     *
     * @param bean 实体对象
     * @param <EntityType> 实体类型
     */
    <EntityType> void deleteEntity(EntityType bean);
}

