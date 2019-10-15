package cn.laoshini.dk.net.msg;

/**
 * @author fagarine
 */
public class ReqCustomMessage<DataType extends ICustomDto> extends ReqMessage<DataType>
        implements ICustomMessage<DataType> {

    @Override
    public String toString() {
        return "ReqCustomMessage{" + "id=" + id + ", code=" + code + ", params='" + params + '\'' + ", data=" + data
                + ", dataType=" + dataType + '}';
    }
}
