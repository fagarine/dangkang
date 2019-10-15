package cn.laoshini.dk.net.msg;

/**
 * 请求消息类型（到达服务器的消息）
 *
 * @author fagarine
 */
public class ReqMessage<Type> extends AbstractMessage<Type> {

    @Override
    public String toString() {
        return "ReqMessage{" + "id=" + id + ", params='" + params + '\'' + ", data=" + data + '}';
    }
}
