package cn.laoshini.dk.net.codec;

import cn.laoshini.dk.domain.GameSubject;

/**
 * 消息编码器接口定义类
 *
 * @param <T> 消息编码后的类型
 * @param <M> 消息编码前的类型
 * @author fagarine
 */
public interface IMessageEncoder<T, M> {

    /**
     * 消息编码
     *
     * @param message 消息体
     * @param subject 消息所属主体对象
     * @return 返回编码后的消息内容
     */
    T encode(M message, GameSubject subject);
}
