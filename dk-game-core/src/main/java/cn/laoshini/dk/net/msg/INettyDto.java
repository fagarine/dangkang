package cn.laoshini.dk.net.msg;

import io.netty.buffer.ByteBuf;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 自定义格式消息数据传输对象接口，专为读写netty的ByteBuf设计，注意：
 * <p>
 * 实现该接口的对象，表示服务器是通过TCP或UDP方式交互，HTTP服务器的消息应该实现{@link ICustomDto}接口
 * </p>
 *
 * @author fagarine
 */
public interface INettyDto extends IByteDto<ByteBuf> {
    /**
     * 子类覆盖此方法从缓冲中读取数据
     *
     * @param b Netty字节缓冲区对象
     */
    @Override
    void read(ByteBuf b);

    /**
     * 子类覆盖此方法将数据写入缓冲
     *
     * @param b Netty字节缓冲区对象
     */
    @Override
    void write(ByteBuf b);

    /**
     * 从缓冲读取Boolean
     *
     * @param buf Netty字节缓冲区对象
     * @return 读取到的 boolean
     */
    @Override
    default boolean readBoolean(ByteBuf buf) {
        int i = buf.readByte();
        return i == 1;
    }

    /**
     * 向缓冲写入Boolean
     *
     * @param buf Netty字节缓冲区对象
     * @param value boolean
     */
    @Override
    default void writeBoolean(ByteBuf buf, boolean value) {
        buf.writeByte(value ? 1 : 0);
    }

    /**
     * 从缓冲读取Byte
     *
     * @param b Netty字节缓冲区对象
     * @return 读取到的 byte
     */
    @Override
    default byte readByte(ByteBuf b) {
        return b.readByte();
    }

    /**
     * 向缓冲写入字节
     *
     * @param b Netty字节缓冲区对象
     * @param v byte
     */
    @Override
    default void writeByte(ByteBuf b, byte v) {
        b.writeByte(v);
    }

    /**
     * 从缓冲读取Byte数组
     *
     * @param b Netty字节缓冲区对象
     * @return 读取到的 byte数组
     */
    @Override
    default byte[] readBytes(ByteBuf b) {
        int length = b.readInt();
        if (length == 0) {
            return new byte[0];
        }
        byte[] bytes = new byte[length];
        b.readBytes(bytes);

        return bytes;
    }

    /**
     * 向缓冲写入字节数组
     *
     * @param b Netty字节缓冲区对象
     * @param v byte数组
     */
    @Override
    default void writeBytes(ByteBuf b, byte[] v) {
        if (v == null || v.length == 0) {
            b.writeInt(0);
            return;
        }

        b.writeInt(v.length);
        b.writeBytes(v);
    }

    /**
     * 从缓冲读取Short
     *
     * @param b Netty字节缓冲区对象
     * @return 读取到的 short
     */
    @Override
    default short readShort(ByteBuf b) {
        return b.readShort();
    }

    /**
     * 向缓冲写入Short
     *
     * @param b Netty字节缓冲区对象
     * @param v short
     */
    @Override
    default void writeShort(ByteBuf b, short v) {
        b.writeShort(v);
    }

    /**
     * 从缓冲读取Int
     *
     * @param b Netty字节缓冲区对象
     * @return 读取到的 int
     */
    @Override
    default int readInt(ByteBuf b) {
        return b.readInt();
    }

    /**
     * 向缓冲写入Int
     *
     * @param b Netty字节缓冲区对象
     * @param v int
     */
    @Override
    default void writeInt(ByteBuf b, int v) {
        b.writeInt(v);
    }

    /**
     * 从缓冲读取Long
     *
     * @param b Netty字节缓冲区对象
     * @return 读取到的 long
     */
    @Override
    default long readLong(ByteBuf b) {
        return b.readLong();
    }

    /**
     * 向缓冲写入Long
     *
     * @param b Netty字节缓冲区对象
     * @param v long
     */
    @Override
    default void writeLong(ByteBuf b, long v) {
        b.writeLong(v);
    }

    /**
     * 从缓冲读取Double
     *
     * @param b Netty字节缓冲区对象
     * @return 返回读取到的double
     */
    @Override
    default double readDouble(ByteBuf b) {
        return b.readDouble();
    }

    /**
     * 向缓冲写入Double
     *
     * @param b Netty字节缓冲区对象
     * @param v double
     */
    @Override
    default void writeDouble(ByteBuf b, double v) {
        b.writeDouble(v);
    }

    /**
     * 从缓冲读取String
     *
     * @param b Netty字节缓冲区对象
     * @return 读取到的 String
     */
    @Override
    default String readString(ByteBuf b) {
        int length = b.readInt();
        if (length <= 0 || b.readableBytes() < length) {
            return null;
        }

        byte[] bytes = new byte[length];
        b.readBytes(bytes);
        return new String(bytes, UTF_8);
    }

}
