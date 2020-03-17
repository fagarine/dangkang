package cn.laoshini.dk.domain.msg;

import cn.laoshini.dk.constant.GameCodeEnum;

/**
 * 返回消息类型，从服务器发出的消息
 *
 * @author fagarine
 */
public class RespMessage<Type> extends AbstractMessage<Type> {

    public static <M> RespMessage<M> noResponse() {
        RespMessage<M> message = new RespMessage<>();
        message.setCode(GameCodeEnum.NO_RESPONSE.getCode());
        return message;
    }

    @Override
    public String toString() {
        return "RespMessage{" + "id=" + id + ", code=" + code + ", params='" + params + '\'' + ", data=" + data
               + ", dataType=" + dataType + '}';
    }
}
