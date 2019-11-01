package cn.laoshini.dk.net.codec;

import cn.laoshini.dk.net.msg.AbstractMessage;
import cn.laoshini.dk.util.MessageUtil;

/**
 * JSON格式消息编码器
 *
 * @author fagarine
 */
public class JsonByteMessageEncoder implements IByteMessageEncoder<AbstractMessage<Object>> {

    @Override
    public byte[] encode(AbstractMessage<Object> message) {
        return MessageUtil.messageToJsonBytes(message);
    }

}
