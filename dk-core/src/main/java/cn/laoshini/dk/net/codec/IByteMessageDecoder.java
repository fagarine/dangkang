package cn.laoshini.dk.net.codec;

import java.util.List;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.MessageLite;

import cn.laoshini.dk.domain.GameSubject;

/**
 * 字节数组类型消息解码器接口，功能单一（只负责解码数据），便于lambda编程
 *
 * @author fagarine
 */
@FunctionalInterface
public interface IByteMessageDecoder<M> extends IMessageDecoder<byte[], List<M>> {

    @Override
    default List<M> decode(byte[] bytes, GameSubject subject) {
        if (bytes == null) {
            throw new IllegalArgumentException("消息数组不能为空");
        }
        return decode(bytes, 0, bytes.length);
    }

    /**
     * 将传入的字节数组解码为指定消息对象
     *
     * @param bytes 字节数组
     * @param off 读取偏移量
     * @param len 读取长度
     * @return 返回解码后的消息对象，以集合形式返回
     */
    List<M> decode(byte[] bytes, int off, int len);

    /**
     * 创建返回一个专用于读取Protobuf消息类的消息解码器，Protobuf3以上版本使用
     *
     * @param prototype Protobuf消息原型
     * @return Protobuf消息解码器
     */
    static ProtobufByteMessageDecoder newProtobufDecoder(MessageLite prototype) {
        return new ProtobufByteMessageDecoder(prototype);
    }

    /**
     * 创建返回一个专用于读取Protobuf消息类的消息解码器
     *
     * @param prototype Protobuf消息原型
     * @param extensionRegistry 如果是protobuf3之前的版本，需要传入消息扩展对象
     * @return Protobuf消息解码器
     */
    static ProtobufByteMessageDecoder newProtobufDecoder(MessageLite prototype, ExtensionRegistry extensionRegistry) {
        return new ProtobufByteMessageDecoder(prototype, extensionRegistry);
    }
}
