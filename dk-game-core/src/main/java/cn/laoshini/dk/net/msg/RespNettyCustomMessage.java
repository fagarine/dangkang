package cn.laoshini.dk.net.msg;

import cn.laoshini.dk.domain.msg.RespMessage;

/**
 * 使用netty通信的自定义格式消息类（从服务器发出的消息）
 *
 * @author fagarine
 */
public class RespNettyCustomMessage<DataType extends INettyDto> extends RespMessage<DataType>
        implements INettyCustomMessage<DataType> {

    @Override
    public String toString() {
        return "RespNettyCustomMessage{" + "id=" + id + ", code=" + code + ", params='" + params + '\'' + ", data="
               + data + ", dataType=" + dataType + '}';
    }
}
