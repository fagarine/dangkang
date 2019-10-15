package cn.laoshini.dk.net.codec;

import cn.laoshini.dk.domain.GameSubject;

/**
 * 以字节数组形式交互的消息编解码器接口
 *
 * @param <MessageType> 消息体类型
 * @author fagarine
 */
public interface IByteMessageCodec<MessageType> extends IMessageCodec<byte[], MessageType> {

    /**
     * 消息解码
     *
     * @param bytes 消息内容
     * @param subject 消息所属主体对象
     * @return 返回解码后的消息体
     */
    @Override
    MessageType decode(byte[] bytes, GameSubject subject);

    /**
     * 消息编码
     *
     * @param message 消息体
     * @param subject 消息所属主体对象
     * @return 返回编码后的消息内容
     */
    @Override
    byte[] encode(MessageType message, GameSubject subject);

}
