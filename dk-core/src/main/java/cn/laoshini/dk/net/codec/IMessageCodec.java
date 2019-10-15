package cn.laoshini.dk.net.codec;

/**
 * 消息编解码器接口定义类
 *
 * @param <T> 消息进入类型
 * @param <M> 消息解码后的类型或编码前的类型
 * @author fagarine
 */
public interface IMessageCodec<T, M> extends IMessageEncoder<T, M>, IMessageDecoder<T, M> {

}
