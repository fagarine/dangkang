package cn.laoshini.dk.client;

import cn.laoshini.dk.domain.msg.AbstractMessage;
import cn.laoshini.dk.net.codec.CustomNettyMessageDecoder;
import cn.laoshini.dk.net.codec.CustomNettyMessageEncoder;
import cn.laoshini.dk.net.codec.INettyMessageDecoder;
import cn.laoshini.dk.net.codec.INettyMessageEncoder;

/**
 * @author fagarine
 */
public class CustomMessageNettyTcpClient<S, M extends AbstractMessage> extends AbstractBiasCodecNettyTcpClient<S, M> {

    @Override
    protected void checkDepends() {
        if (messageEncoder() == null) {
            setMessageEncoder((INettyMessageEncoder<M>) new CustomNettyMessageEncoder());
        }
        if (messageDecoder() == null) {
            setMessageDecoder((INettyMessageDecoder<M>) new CustomNettyMessageDecoder());
        }

        super.checkDepends();

        if (idReader() == null) {
            setIdReader(AbstractMessage::getId);
        }
    }
}
