package cn.laoshini.dk.net.msg;

import cn.laoshini.dk.domain.msg.ReqMessage;

/**
 * 使用netty通信的自定义格式消息类（到达服务器的消息）
 *
 * @author fagarine
 */
public class ReqNettyCustomMessage<DataType extends INettyDto> extends ReqMessage<DataType>
        implements INettyCustomMessage<DataType> {

    @Override
    public String toString() {
        return "ReqNettyCustomMessage{" + "id=" + id + ", params='" + params + '\'' + ", data=" + data + ", dataType="
               + dataType + '}';
    }
}
