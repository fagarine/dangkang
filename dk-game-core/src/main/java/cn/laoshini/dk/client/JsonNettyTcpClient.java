package cn.laoshini.dk.client;

import cn.laoshini.dk.net.codec.INettyMessageDecoder;
import cn.laoshini.dk.net.codec.INettyMessageEncoder;
import cn.laoshini.dk.net.codec.JsonNettyMessageDecoder;
import cn.laoshini.dk.net.codec.JsonNettyMessageEncoder;
import cn.laoshini.dk.net.msg.AbstractMessage;

/**
 * 使用netty + json格式消息通信的TCP客户端实现类，使用该类时，用户如果不单独设置编解码器，
 * 则默认使用{@link JsonNettyMessageEncoder}和{@link JsonNettyMessageDecoder}
 *
 * @author fagarine
 */
public class JsonNettyTcpClient<S, M extends AbstractMessage> extends AbstractBiasCodecNettyTcpClient<S, M> {

    @Override
    protected void checkDepends() {
        if (messageEncoder() == null) {
            setMessageEncoder((INettyMessageEncoder<M>) new JsonNettyMessageEncoder());
        }
        if (messageDecoder() == null) {
            setMessageDecoder((INettyMessageDecoder<M>) new JsonNettyMessageDecoder());
        }

        super.checkDepends();

        if (idReader() == null) {
            setIdReader(AbstractMessage::getId);
        }
    }

}
