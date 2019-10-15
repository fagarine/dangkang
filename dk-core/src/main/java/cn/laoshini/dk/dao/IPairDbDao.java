package cn.laoshini.dk.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.laoshini.dk.annotation.ConfigurableFunction;
import cn.laoshini.dk.dao.query.BeanQueryCondition;
import cn.laoshini.dk.dao.query.ListQueryCondition;
import cn.laoshini.dk.serialization.IDataSerializable;
import cn.laoshini.dk.util.ReflectUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 键值对数据库公用访问接口
 *
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.pair.dao")
public interface IPairDbDao extends IBasicDao, IPairConditional {

    /**
     * 默认序列化key的方法
     *
     * @param key key
     * @return 返回序列化后的byte数组，该方法不会返回null
     */
    default byte[] keyToBytes(String key) {
        return (key == null) ? IDataSerializable.EMPTY_BYTES : key.getBytes(UTF_8);
    }

    /**
     * 默认反序列化key方法
     *
     * @param bytes byte数组
     * @return 该方法可能返回null
     */
    default String bytesToKey(byte[] bytes) {
        return (bytes == null) ? null : new String(bytes, UTF_8);
    }

    /**
     * 获取键值对中的value序列化操作工具
     *
     * @return 返回序列化操作工具
     */
    IDataSerializable getValueSerialization();

    /**
     * 保存键值对数据
     *
     * @param key key
     * @param value value
     */
    void saveKeyValue(Object key, Object value);

    /**
     * 保存Map中的所有数据
     *
     * @param map 待保存的数据
     */
    void saveMap(Map<String, Object> map);

    /**
     * 获取数据对象的值，以byte数组形式返回
     *
     * @param key key
     * @return 返回byte数组的数据，该方法应保证不会返回null
     */
    byte[] getBytes(String key);

    /**
     * 获取数据对象的值，以字符串形式返回（仅用于字符串格式的值）
     *
     * @param key key
     * @return 返回字符串，该方法应保证不会返回null
     */
    default String getByString(String key) {
        byte[] bytes = getBytes(key);
        return bytesToKey(bytes);
    }

    /**
     * 查找数据，并以指定类型返回
     *
     * @param key key
     * @param toType 指定返回数据类型Class
     * @param <T> 返回数据类型
     * @return 返回数据类型的数据
     */
    default <T> T selectByKey(String key, Class<T> toType) {
        return getValueSerialization().toAssignedTypeObject(getBytes(key), toType);
    }

    /**
     * 根据查询条件查找数据，并以指定类型返回
     *
     * @param queryCondition 查询条件
     * @param toType 指定返回数据类型Class
     * @param <T> 返回数据类型
     * @return 返回数据类型的数据
     */
    default <T> T selectByCondition(BeanQueryCondition queryCondition, Class<T> toType) {
        return selectByKey(toKey(queryCondition), toType);
    }

    /**
     * 查找数据，并以指定类型的集合形式返回，用于一个key下保存了一个集合的数据返回
     *
     * @param key key
     * @param toType 指定返回数据类型Class
     * @param <T> 返回数据类型
     * @return 该方法不会返回null
     */
    default <T> List<T> selectListByKey(String key, Class<T> toType) {
        return getValueSerialization().toAssignedBeanList(getBytes(key), toType);
    }

    /**
     * 根据查询条件查找数据，并以指定类型的集合返回
     *
     * @param queryCondition 查询条件
     * @param toType 指定返回数据类型Class
     * @param <T> 返回数据类型
     * @return 该方法不会返回null
     */
    default <T> List<T> selectListByCondition(ListQueryCondition queryCondition, Class<T> toType) {
        String key = toRegExp(queryCondition);
        Map<String, T> map = selectByRegExp(key, toType);
        List<T> list = new ArrayList<>(map.values());
        List<T> result = new ArrayList<>();
        // 结果过滤
        for (T bean : list) {
            if (ReflectUtil.containsAssignedValueFields(bean, queryCondition.getFilters())) {
                result.add(bean);
            }
        }
        return result;
    }

    /**
     * 根据正则表达式匹配key，查找所有符合条件的数据
     *
     * @param regExp key前缀
     * @param toType 指定返回数据类型Class
     * @param <T> 返回数据类型
     * @return 该方法不会返回null
     */
    <T> Map<String, T> selectByRegExp(String regExp, Class<T> toType);

    /**
     * 删除指定key的值
     *
     * @param key key
     */
    void deleteByKey(String key);
}
