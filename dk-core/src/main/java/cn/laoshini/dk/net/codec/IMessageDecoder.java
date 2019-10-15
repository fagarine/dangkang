package cn.laoshini.dk.net.codec;

import cn.laoshini.dk.domain.GameSubject;

/**
 * 消息解码器接口定义类
 *
 * @param <T> 消息解码前的类型
 * @param <M> 消息解码后的类型
 * @author fagarine
 */
public interface IMessageDecoder<T, M> {

    /**
     * 消息解码
     *
     * @param data 消息内容
     * @param subject 消息所属主体对象
     * @return 返回解码后的消息体
     */
    M decode(T data, GameSubject subject);
}
