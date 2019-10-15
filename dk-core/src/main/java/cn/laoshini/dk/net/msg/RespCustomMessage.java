package cn.laoshini.dk.net.msg;

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
