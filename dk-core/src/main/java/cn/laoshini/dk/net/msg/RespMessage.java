package cn.laoshini.dk.net.msg;

/**
 * 返回消息类型，从服务器发出的消息
 *
 * @author fagarine
 */
public class RespMessage<Type> extends AbstractMessage<Type> {

    @Override
    public String toString() {
        return "RespMessage{" + "id=" + id + ", code=" + code + ", params='" + params + '\'' + ", data=" + data
                + ", dataType=" + dataType + '}';
    }
}
