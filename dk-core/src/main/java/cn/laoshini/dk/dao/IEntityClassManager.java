package cn.laoshini.dk.dao;

import java.util.List;
import java.util.Map;

import cn.laoshini.dk.annotation.ConfigurableFunction;

/**
 * 实体类管理
 *
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.rdb.entity-manager")
public interface IEntityClassManager {

    /**
     * 注册表与实体类的映射关系
     *
     * @param tableName 表名
     * @param tableClass 实体类
     */
    void registerEntityClass(String tableName, Class<?> tableClass);

    /**
     * 批量注册
     *
     * @param map key:表名称，value:对应的类
     */
    void batchRegister(Map<String, Class<?>> map);

    /**
     * 预备批量注销
     *
     * @param classLoader 类加载器
     */
    void prepareUnregister(ClassLoader classLoader);

    /**
     * 批量注销
     */
    void unregister();

    boolean containsClass(Class<?> clazz);

    String getClassTableName(String className);

    Class<?> getTableBeanClass(String tableName);

    List<String> getTableNames();

}
