package cn.laoshini.dk.net.codec;

import cn.laoshini.dk.domain.GameSubject;

/**
 * 将消息编码成字节数组的编码器接口，功能单一（只负责消息编码），便于lambda编程
 *
 * @author fagarine
 */
@FunctionalInterface
public interface IByteMessageEncoder<M> extends IMessageEncoder<byte[], M> {

    /**
     * 创建并返回一个新的Protobuf消息编码器
     *
     * @return 返回Protobuf消息编码器
     */
    static ProtobufByteMessageEncoder newProtobufEncoder() {
        return new ProtobufByteMessageEncoder();
    }

    @Override
    default byte[] encode(M message, GameSubject subject) {
        return encode(message);
    }

    /**
     * 将传入消息对象编码成字节数组并返回
     *
     * @param message 消息内容
     * @return 返回编码后的数据，允许返回null
     */
    byte[] encode(M message);
}
