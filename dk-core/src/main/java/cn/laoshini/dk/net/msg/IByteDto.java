package cn.laoshini.dk.net.msg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.util.CollectionUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 与字节数据转换的DTO对象接口
 *
 * @param <BufferType> 字节缓冲区类型
 * @author fagarine
 */
public interface IByteDto<BufferType> extends Serializable {

    /**
     * 记录该接口支持的数据类型
     */
    List<String> SUPPORTED_TYPES = Arrays
            .asList(boolean.class.getSimpleName(), byte.class.getSimpleName(), byte[].class.getSimpleName(),
                    short.class.getSimpleName(), int.class.getSimpleName(), long.class.getSimpleName(),
                    double.class.getSimpleName(), String.class.getSimpleName(), IByteDto.class.getSimpleName());

    /**
     * 从缓冲中读取数据
     *
     * @param buf 字节缓冲区对象
     */
    void read(BufferType buf);

    /**
     * 将数据写入缓冲
     *
     * @param buf 字节缓冲区对象
     */
    void write(BufferType buf);

    /**
     * 从缓冲读取Boolean
     *
     * @param buf 字节缓冲区对象
     * @return 返回读取到的boolean
     */
    boolean readBoolean(BufferType buf);

    /**
     * 向缓冲写入Boolean
     *
     * @param buf 字节缓冲区对象
     * @param value boolean
     */
    void writeBoolean(BufferType buf, boolean value);

    /**
     * 从缓冲读取Byte
     *
     * @param buf 字节缓冲区对象
     * @return 返回读取到的byte
     */
    byte readByte(BufferType buf);

    /**
     * 向缓冲写入字节
     *
     * @param buf 字节缓冲区对象
     * @param v byte
     */
    void writeByte(BufferType buf, byte v);

    /**
     * 从缓冲读取Byte数组
     *
     * @param buf 字节缓冲区对象
     * @return 返回读取到的byte数组
     */
    byte[] readBytes(BufferType buf);

    /**
     * 向缓冲写入字节数组
     *
     * @param buf 字节缓冲区对象
     * @param v byte数组
     */
    void writeBytes(BufferType buf, byte[] v);

    /**
     * 从缓冲读取Short
     *
     * @param buf 字节缓冲区对象
     * @return 返回读取到的short
     */
    short readShort(BufferType buf);

    /**
     * 向缓冲写入Short
     *
     * @param buf 字节缓冲区对象
     * @param v short
     */
    void writeShort(BufferType buf, short v);

    /**
     * 从缓冲读取Int
     *
     * @param buf 字节缓冲区对象
     * @return 返回读取到的int
     */
    int readInt(BufferType buf);

    /**
     * 向缓冲写入Int
     *
     * @param buf 字节缓冲区对象
     * @param v int
     */
    void writeInt(BufferType buf, int v);

    /**
     * 从缓冲读取Long
     *
     * @param buf 字节缓冲区对象
     * @return 返回读取到的long
     */
    long readLong(BufferType buf);

    /**
     * 向缓冲写入Long
     *
     * @param buf 字节缓冲区对象
     * @param v long
     */
    void writeLong(BufferType buf, long v);

    /**
     * 从缓冲读取Double
     *
     * @param buf 字节缓冲区对象
     * @return 返回读取到的double
     */
    double readDouble(BufferType buf);

    /**
     * 向缓冲写入Double
     *
     * @param buf 字节缓冲区对象
     * @param v double
     */
    void writeDouble(BufferType buf, double v);

    /**
     * 从缓冲读取String
     *
     * @param buf 字节缓冲区对象
     * @return 返回读取到的String
     */
    String readString(BufferType buf);

    /**
     * 向缓冲写入String
     *
     * @param buf 字节缓冲区对象
     * @param v String
     */
    default void writeString(BufferType buf, String v) {
        if (v == null) {
            writeInt(buf, 0);
        } else {
            byte[] bytes = v.getBytes(UTF_8);
            writeBytes(buf, bytes);
        }
    }

    /**
     * 从缓冲读取对象
     *
     * @param buf 字节缓冲区对象
     * @param beanClass 要读取的对象的类型，必须是{@link IByteDto}的子类
     * @param check 是否需要读取检查信息（检查bean是否为空，一个字节的长度），单个JavaBean需要，集合中的不需要
     * @return 返回读取到的对象，该方法可能返回null
     */
    default <T extends IByteDto<BufferType>> T readBean(BufferType buf, Class<T> beanClass, boolean check) {
        if ((!check || readBoolean(buf)) && beanClass != null) {
            try {
                T bean = beanClass.newInstance();
                bean.read(buf);
                return bean;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new MessageException(GameCodeEnum.PARAM_ERROR, "build.dto.fail",
                        "创建IByteDto实例出错:" + beanClass.getName());
            }
        }
        return null;
    }

    /**
     * 根据指定类型读取数据（不包含集合类型）
     *
     * @param buf 字节缓冲区对象
     * @param type 数据类型
     * @return 该方法可能返回null
     */
    default Object readByType(BufferType buf, Class<?> type) {
        if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            return readBoolean(buf);
        } else if (Byte.class.equals(type) || byte.class.equals(type)) {
            return readByte(buf);
        } else if (Short.class.equals(type) || short.class.equals(type)) {
            return readShort(buf);
        } else if (Integer.class.equals(type) || int.class.equals(type)) {
            return readInt(buf);
        } else if ((Long.class.equals(type)) || long.class.equals(type)) {
            return readLong(buf);
        } else if ((Double.class.equals(type)) || double.class.equals(type)) {
            return readDouble(buf);
        } else if (String.class.equals(type)) {
            return readString(buf);
        } else if (IByteDto.class.isAssignableFrom(type)) {
            return readBean(buf, (Class<? extends IByteDto<BufferType>>) type, true);
        }

        throw new MessageException(GameCodeEnum.PARAM_ERROR, "custom.param.error",
                String.format("自定义格式消息中，不支持[%s]类型的读写，支持的类型有:%s", type.getName(), SUPPORTED_TYPES));
    }

    /**
     * 从缓冲读取一个集合的内容
     *
     * @param buf 字节缓冲区对象
     * @param beanClass 要读取的对象的类型，必须是基本类型或{@link IByteDto}的子类
     * @param <T> 要读取的对象的类型
     * @return 该方法不会返回null
     */
    default <T> List<T> readList(BufferType buf, Class<T> beanClass) {
        if (beanClass != null) {
            // 读取集合的长度
            short size = readShort(buf);

            List<T> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add((T) readByType(buf, beanClass));
            }
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * 向缓冲写入{@link IByteDto}对象
     *
     * @param buf 字节缓冲区对象
     * @param bean 要写入的对象的类型，必须是{@link IByteDto}的子类
     * @param check 是否需要写入检查信息（检查bean是否为空，一个字节的长度），单个JavaBean需要，集合中的不需要
     */
    default void writeBean(BufferType buf, IByteDto<BufferType> bean, boolean check) {
        if (check) {
            writeBoolean(buf, bean != null);
        }

        if (bean != null) {
            bean.write(buf);
        }
    }

    /**
     * 向缓冲写入一个对象，基本类型或{@link IByteDto}的子类或这些类型的集合
     *
     * @param buf 字节缓冲区对象
     * @param obj 要写入的数据
     * @param check 如果是{@link IByteDto}的子类对象，是否写入检查信息（如果对象来自于集合中，则为false）
     */
    default void writeByType(BufferType buf, Object obj, boolean check) {
        if (obj == null) {
            if (check) {
                writeBoolean(buf, check);
            }
            return;
        }

        Class<?> type = obj.getClass();
        if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            writeBoolean(buf, (Boolean) obj);
        } else if (Byte.class.equals(type) || byte.class.equals(type)) {
            writeByte(buf, (Byte) obj);
        } else if (Short.class.equals(type) || short.class.equals(type)) {
            writeShort(buf, (Short) obj);
        } else if (Integer.class.equals(type) || int.class.equals(type)) {
            writeInt(buf, (Integer) obj);
        } else if ((Long.class.equals(type)) || long.class.equals(type)) {
            writeLong(buf, (Long) obj);
        } else if ((Double.class.equals(type)) || double.class.equals(type)) {
            writeDouble(buf, (Double) obj);
        } else if (String.class.equals(type)) {
            writeString(buf, (String) obj);
        } else if (IByteDto.class.isAssignableFrom(type)) {
            writeBean(buf, (IByteDto<BufferType>) obj, check);
        } else if (List.class.isAssignableFrom(type)) {
            writeList(buf, (List<?>) obj);
        } else {
            throw new MessageException(GameCodeEnum.PARAM_ERROR, "custom.param.error",
                    String.format("自定义格式消息中，不支持[%s]类型的读写，支持的类型有:%s", type.getName(), SUPPORTED_TYPES));
        }

    }

    /**
     * 向缓冲写入一个对象，基本类型或{@link IByteDto}的子类或这些类型的集合
     *
     * @param buf 字节缓冲区对象
     * @param obj 要写入的数据
     */
    default void writeObj(BufferType buf, Object obj) {
        writeByType(buf, obj, true);
    }

    /**
     * 向缓冲写入集合中的数据
     *
     * @param buf 字节缓冲区对象
     * @param list 要写入的数据集合
     * @param <T> 数据类型，必须是基本类型或{@link IByteDto}的子类
     */
    default <T> void writeList(BufferType buf, List<T> list) {
        if (CollectionUtil.isEmpty(list)) {
            writeShort(buf, (short) 0);
            return;
        }

        short size = (short) list.size();
        writeShort(buf, size);
        for (T t : list) {
            writeByType(buf, t, false);
        }
    }

    /**
     * 单个参数的hashcode最大值
     */
    int MAX_HASH_CODE = Integer.MAX_VALUE >> 4;

    /**
     * 默认的hashcode计算方法
     *
     * @param object 待计算的对象
     * @return 返回计算后的hashcode
     */
    default int hashCode(Object object) {
        if (object == null) {
            return 0;
        }

        int code = 0;
        Class<?> type = object.getClass();
        if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            code = ((Boolean) object) ? 1 : 0;
        } else if (Byte.class.equals(type) || byte.class.equals(type) || Short.class.equals(type) || short.class
                .equals(type) || Integer.class.equals(type) || int.class.equals(type) || Double.class.equals(type)
                || double.class.equals(type) || (Long.class.equals(type)) || long.class.equals(type)) {
            code = (Integer) object;
        } else if (String.class.equals(type)) {
            byte[] bytes = ((String) object).getBytes(UTF_8);
            for (byte b : bytes) {
                code += b;
            }
        } else {
            code = object.hashCode();
        }
        return code % MAX_HASH_CODE;
    }
}
