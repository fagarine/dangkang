package cn.laoshini.dk.serialization;

import java.util.List;

/**
 * 数据序列化相关功能接口
 *
 * @author fagarine
 */
public interface IDataSerializable {

    /**
     * byte空数组
     */
    byte[] EMPTY_BYTES = new byte[0];

    /**
     * 将数据以二进制数据形式输出
     *
     * @param object 待序列化数据
     * @return 返回二进制数组格式的数据
     */
    byte[] toBytes(Object object);

    /**
     * 将二进制数据反序列化成对象输出
     *
     * @param bytes 二进制数据
     * @return 返回对象
     */
    Object toObject(byte[] bytes);

    /**
     * 将二进制数据反序列化成指定类型对象输出
     *
     * @param bytes 二进制数据
     * @param toType 指定类型
     * @param <T> 指定类型
     * @return 返回对象
     */
    <T> T toAssignedTypeObject(byte[] bytes, Class<T> toType);

    /**
     * 将二进制数据反序列化成指定类型对象的集合输出
     *
     * @param bytes 二进制数据
     * @param toType 指定类型
     * @param <T> 指定类型
     * @return 返回对象集合，该方法不会返回null
     */
    <T> List<T> toAssignedBeanList(byte[] bytes, Class<T> toType);
}
