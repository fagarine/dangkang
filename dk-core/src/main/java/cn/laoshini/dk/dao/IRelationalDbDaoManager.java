package cn.laoshini.dk.dao;

import cn.laoshini.dk.annotation.ConfigurableFunction;

/**
 * 关系数据库数据访问对象管理接口
 *
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.rdb.manager", description = "关系数据库数据访问对象管理")
public interface IRelationalDbDaoManager {

    /**
     * 获取有效的数据库访问对象
     *
     * @param tableName 表名
     * @param clazz 实体类类型
     * @param <EntityType> 实体类类型
     * @return 该方法不会返回null
     */
    <EntityType> IRelationalDbDao<EntityType> getValidDbDao(String tableName, Class<EntityType> clazz);

    /**
     * 验证表是否存在
     *
     * @param tableName 表名
     * @return 验证结果
     */
    boolean validateTable(String tableName);
}
