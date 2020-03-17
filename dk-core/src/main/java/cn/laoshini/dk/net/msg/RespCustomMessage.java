package cn.laoshini.dk.net.msg;

import cn.laoshini.dk.domain.msg.RespMessage;

/**
 * @author fagarine
 */
public class RespCustomMessage<DataType extends ICustomDto> extends RespMessage<DataType>
        implements ICustomMessage<DataType> {

    @Override
    public String toString() {
        return "RespCustomMessage{" + "id=" + id + ", code=" + code + ", params='" + params + '\'' + ", data=" + data
               + ", dataType=" + dataType + '}';
    }
}
