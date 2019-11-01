package cn.laoshini.dk.client;

import cn.laoshini.dk.net.codec.INettyMessageDecoder;
import cn.laoshini.dk.net.codec.INettyMessageEncoder;
import cn.laoshini.dk.util.LogUtil;

/**
 * 带有明确偏向编解码器的客户端抽象类，这样的类会使用指定的消息编解码器，所以来自父类的编解码器设置方法不再需要被用户单独调用
 *
 * @author fagarine
 */
public abstract class AbstractBiasCodecNettyTcpClient<S, M> extends AbstractNettyTcpClient<S, M> {

    @Override
    public final AbstractBiasCodecNettyTcpClient<S, M> setMessageEncoder(INettyMessageEncoder<M> messageEncoder) {
        if (messageEncoder() != null) {
            LogUtil.error("消息编码器已存在，原编码器被覆盖！！！");
        } else {
            super.setMessageEncoder(messageEncoder);
        }
        return this;
    }

    @Override
    public final AbstractBiasCodecNettyTcpClient<S, M> setMessageDecoder(INettyMessageDecoder<M> messageDecoder) {
        if (messageDecoder() != null) {
            LogUtil.error("消息解码器已存在，原解码器被覆盖！！！");
        } else {
            super.setMessageDecoder(messageDecoder);
        }
        return this;
    }
}
