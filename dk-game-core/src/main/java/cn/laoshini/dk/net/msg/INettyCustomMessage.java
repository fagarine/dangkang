package cn.laoshini.dk.net.msg;

import io.netty.buffer.ByteBuf;

import cn.laoshini.dk.domain.msg.IMessage;

/**
 * 自定义消息类型接口，专为读写netty的ByteBuf设计，注意：
 * <p>
 * 实现该接口的对象，表示服务器是通过Netty交互，HTTP服务器的消息应该实现{@link ICustomMessage}接口
 * </p>
 * <br>
 * 另外，该类的消息结构完全继承{@link ICustomMessage}中定义的消息结构，具体参见{@link ICustomMessage}接口
 *
 * @author fagarine
 * @see ICustomMessage
 * @see CustomMsg
 */
public interface INettyCustomMessage<Type extends INettyDto> extends INettyDto, IMessage<Type> {

    @Override
    default void read(ByteBuf b) {
        setId(readInt(b));
        setCode(readInt(b));
        setParams(readString(b));
        setData(readBean(b, getDataType(), false));
    }

    @Override
    default void write(ByteBuf b) {
        writeInt(b, getId());
        writeInt(b, getCode());
        writeString(b, getParams());
        writeBean(b, getData(), false);
    }
}
