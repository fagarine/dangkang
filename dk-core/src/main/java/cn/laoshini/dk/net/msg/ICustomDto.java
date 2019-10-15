package cn.laoshini.dk.net.msg;

import java.nio.ByteBuffer;
import java.util.List;

import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.exception.MessageException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 自定义格式消息的数据传输对象接口，即调用{@link ICustomMessage#getData()}方法返回的消息内容对象的类型
 * <p>
 * 注意：该类的读写基于JDK自带的字节缓冲区对象{@link ByteBuffer}，并不适合直接用于与Netty交互的数据的读写
 * </p>
 *
 * @author fagarine
 */
public interface ICustomDto extends IByteDto<ByteBuffer> {

    /**
     * 从缓冲中读取数据
     *
     * @param b JDK自带的字节缓冲区对象
     */
    @Override
    void read(ByteBuffer b);

    /**
     * 将数据写入缓冲
     *
     * @param b JDK自带的字节缓冲区对象
     */
    @Override
    void write(ByteBuffer b);

    /**
     * 从缓冲读取Boolean
     *
     * @param buf JDK自带的字节缓冲区对象
     * @return 返回读取到的boolean
     */
    @Override
    default boolean readBoolean(ByteBuffer buf) {
        return buf.get() == 1;
    }

    /**
     * 向缓冲写入Boolean
     *
     * @param buf JDK自带的字节缓冲区对象
     * @param value boolean
     */
    @Override
    default void writeBoolean(ByteBuffer buf, boolean value) {
        buf.put((byte) (value ? 1 : 0));
    }

    /**
     * 从缓冲读取Byte
     *
     * @param b JDK自带的字节缓冲区对象
     * @return 返回读取到的byte
     */
    @Override
    default byte readByte(ByteBuffer b) {
        return b.get();
    }

    /**
     * 向缓冲写入字节
     *
     * @param b JDK自带的字节缓冲区对象
     * @param v byte
     */
    @Override
    default void writeByte(ByteBuffer b, byte v) {
        b.put(v);
    }

    /**
     * 从缓冲读取Byte数组
     *
     * @param b JDK自带的字节缓冲区对象
     * @return 返回读取到的byte数组
     */
    @Override
    default byte[] readBytes(ByteBuffer b) {
        int length = b.getInt();
        if (length == 0) {
            return new byte[0];
        }

        byte[] bytes = new byte[length];
        b.get(bytes);
        return bytes;
    }

    /**
     * 向缓冲写入字节数组
     *
     * @param b JDK自带的字节缓冲区对象
     * @param v byte数组
     */
    @Override
    default void writeBytes(ByteBuffer b, byte[] v) {
        if (v == null || v.length == 0) {
            b.putInt(0);
            return;
        }

        b.putInt(v.length);
        b.put(v);
    }

    /**
     * 从缓冲读取Short
     *
     * @param b JDK自带的字节缓冲区对象
     * @return 返回读取到的short
     */
    @Override
    default short readShort(ByteBuffer b) {
        return b.getShort();
    }

    /**
     * 向缓冲写入Short
     *
     * @param b JDK自带的字节缓冲区对象
     * @param v short
     */
    @Override
    default void writeShort(ByteBuffer b, short v) {
        b.putShort(v);
    }

    /**
     * 从缓冲读取Int
     *
     * @param b JDK自带的字节缓冲区对象
     * @return 返回读取到的int
     */
    @Override
    default int readInt(ByteBuffer b) {
        return b.getInt();
    }

    /**
     * 向缓冲写入Int
     *
     * @param b JDK自带的字节缓冲区对象
     * @param v int
     */
    @Override
    default void writeInt(ByteBuffer b, int v) {
        b.putInt(v);
    }

    /**
     * 从缓冲读取Long
     *
     * @param b JDK自带的字节缓冲区对象
     * @return 返回读取到的long
     */
    @Override
    default long readLong(ByteBuffer b) {
        return b.getLong();
    }

    /**
     * 向缓冲写入Long
     *
     * @param b JDK自带的字节缓冲区对象
     * @param v long
     */
    @Override
    default void writeLong(ByteBuffer b, long v) {
        b.putLong(v);
    }

    /**
     * 从缓冲读取Double
     *
     * @param b JDK自带的字节缓冲区对象
     * @return 返回读取到的double
     */
    @Override
    default double readDouble(ByteBuffer b) {
        return b.getDouble();
    }

    /**
     * 向缓冲写入Double
     *
     * @param b JDK自带的字节缓冲区对象
     * @param v double
     */
    @Override
    default void writeDouble(ByteBuffer b, double v) {
        b.putDouble(v);
    }

    /**
     * 从缓冲读取String
     *
     * @param b JDK自带的字节缓冲区对象
     * @return 返回读取到的String
     */
    @Override
    default String readString(ByteBuffer b) {
        int length = b.getInt();
        if (length <= 0 || b.remaining() < length) {
            return null;
        }

        byte[] bytes = new byte[length];
        b.get(bytes);
        return new String(bytes, UTF_8);
    }

    /**
     * 返回对象转换为字节数组后的长度，用于申请缓冲区大小时的参考
     * <p>
     * 由于JDK自带的字节缓冲区对象{@link ByteBuffer}不能在写数据时自动扩容，需要提前计算容量
     * </p>
     * 由于手动创建的DTO类可能会导致该方法返回的长度数值有误，使得该方法失去意义，且浪费缓冲区资源，
     * 强烈建议本类的实现类使用生成工具生成，避免出现这类错误
     *
     * @return 返回对象长度
     */
    int byteSize();

    /**
     * 计算对象转换为二进制数组后的长度
     *
     * @param obj 对象
     * @return 返回长度
     */
    default int byteSize(Object obj) {
        int size = 0;
        if (obj != null) {
            Class<?> type = obj.getClass();
            if (Boolean.class.equals(type) || boolean.class.equals(type) || Byte.class.equals(type) || byte.class
                    .equals(type)) {
                size = Byte.BYTES;
            } else if (Short.class.equals(type) || short.class.equals(type)) {
                size = Short.BYTES;
            } else if (Integer.class.equals(type) || int.class.equals(type)) {
                size = Integer.BYTES;
            } else if ((Long.class.equals(type)) || long.class.equals(type)) {
                size = Long.BYTES;
            } else if ((Double.class.equals(type)) || double.class.equals(type)) {
                size = Double.BYTES;
            } else if (String.class.equals(type)) {
                size = Integer.BYTES + ((String) obj).getBytes(UTF_8).length;
            } else if (ICustomDto.class.isAssignableFrom(type)) {
                size = Byte.BYTES + ((ICustomDto) obj).byteSize();
            } else if (List.class.isAssignableFrom(type)) {
                List<?> list = (List<?>) obj;
                size += Short.BYTES;
                for (Object o : list) {
                    size += byteSize(o);
                }
            } else {
                throw new MessageException(GameCodeEnum.PARAM_ERROR, "custom.param.error",
                        String.format("自定义格式消息中，不支持[%s]类型的读写，支持的类型有:%s", type.getName(), SUPPORTED_TYPES));
            }
        }
        return size;
    }

}
