package cn.laoshini.dk.dao;

import java.util.List;
import java.util.Map;

import cn.laoshini.dk.annotation.ConfigurableFunction;
import cn.laoshini.dk.dao.query.BeanQueryCondition;
import cn.laoshini.dk.dao.query.ListQueryCondition;
import cn.laoshini.dk.dao.query.Page;
import cn.laoshini.dk.dao.query.PageQueryCondition;
import cn.laoshini.dk.exception.DaoException;

/**
 * 关系数据库公用访问接口
 * <p>
 * 注意：由于表与实体类对象的映射使用的是反射，表中的字段类型，必须与实体类中的变量类型保持一致
 * </p>
 *
 * @param <EntityType> 表数据类型，数据库表对应的实体类，实体类必须使用@{@link TableMapping}标记
 * @author fagarine
 * @see TableMapping
 */
@ConfigurableFunction(key = "dk.rdb.dao")
public interface IRelationalDbDao<EntityType> extends IBasicDao {

    /**
     * 返回泛型类型
     *
     * @return 该方法不允许返回空
     */
    Class<EntityType> getType();

    /**
     * 返回表名称
     *
     * @return 该方法可能返回空
     */
    String getTableName();

    /**
     * 保存数据
     *
     * @param data 数据对象
     * @return 返回影响行数
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    int insert(EntityType data) throws DaoException;

    /**
     * 保存list中的所有数据
     *
     * @param list 待保存的数据
     * @return 返回影响行数
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    int insertList(List<EntityType> list) throws DaoException;

    /**
     * 更新数据到数据库
     *
     * @param entity 数据对象
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    int update(EntityType entity) throws DaoException;

    /**
     * 批量更新数据到数据类
     *
     * @param entities 待保存的数据
     * @return 返回影响行数
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    int updateList(List<EntityType> entities) throws DaoException;

    /**
     * 批量更新数据
     *
     * @param updatedColumns 要更新的内容，key: 字段名（类中记录的字段名）, value: 更新后的值
     * @param condition 查找条件，同上
     * @return 返回影响行数
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    int batchUpdate(Map<String, Object> updatedColumns, Map<String, Object> condition) throws DaoException;

    /**
     * 全量更新表中所有行的数据
     *
     * @param updatedColumns 要更新的内容，key: 字段名（类中记录的字段名）, value: 更新后的值
     * @return 返回影响行数
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    int fullUpdate(Map<String, Object> updatedColumns) throws DaoException;

    /**
     * 删除实体对象（物理删除，慎用）
     *
     * @param entity 数据对象
     * @return 返回影响行数
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    int delete(EntityType entity) throws DaoException;

    /**
     * 查询单条数据
     *
     * @param queryCondition 查询条件
     * @return 返回查询结果
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    EntityType selectByCondition(BeanQueryCondition queryCondition) throws DaoException;

    /**
     * 查询多条数据，并以List姓氏返回
     *
     * @param queryCondition 查询条件
     * @return 返回查询结果
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    List<EntityType> selectListByCondition(ListQueryCondition queryCondition) throws DaoException;

    /**
     * 分页查询
     *
     * @param queryCondition 查询条件
     * @return 返回查询结果
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    Page<EntityType> selectByPage(PageQueryCondition queryCondition) throws DaoException;

    /**
     * 查询表中所有数据
     *
     * @return 返回查询结果
     * @throws DaoException 如果执行出错，将会抛出异常
     */
    List<EntityType> selectAll() throws DaoException;
}
