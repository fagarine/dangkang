package cn.laoshini.dk.net.msg;

import java.nio.ByteBuffer;

import cn.laoshini.dk.constant.GameConstant;
import cn.laoshini.dk.net.ICustomMessageFactory;

/**
 * 自定义消息类型设计接口，实现该接口表示实现类为一个自定义格式的消息类，同时该类必须添加@{@link CustomMsg}注解，
 * 消息类的发现、注册和实例创建由系统完成
 * <p>
 * 服务器接收消息格式：长度（int） + 校验码（int） + 消息id（int） ＋ 消息体（字节数组，不会压缩）<br>
 * 服务器发出消息格式：长度（int） + 消息id（int） ＋ 消息体（字节数组，可能会压缩）<br>
 * 其中消息体格式：消息id（int） + 消息返回码（int，应答消息使用，请求消息为0） + 扩展预留字段（String） + 消息具体内容（可能为空）
 * </p>
 * 消息最大长度与常量{@link GameConstant#MAX_FRAME_LENGTH}相关
 * <p>
 * 需要注意的是：该类的读写都基于JDK自带的字节缓冲区对象{@link ByteBuffer}，并不适合直接用于与Netty交互的数据的读写；<br>
 * 设计该类的主要用途是与使用ByteBuffer做为缓冲区的服务器（如使用JDK的socket编程实现的游戏服，或者使用HTTP通信的游戏服等）交互的消息的读写
 * </p>
 *
 * @author fagarine
 * @see ICustomMessageFactory
 * @see CustomMsg
 * @see ICustomDto
 */
public interface ICustomMessage<Type extends ICustomDto> extends ICustomDto, IMessage<Type> {

    @Override
    default void read(ByteBuffer b) {
        setId(readInt(b));
        setCode(readInt(b));
        setParams(readString(b));
        setData(readBean(b, getDataType(), true));
    }

    @Override
    default void write(ByteBuffer b) {
        writeInt(b, getId());
        writeInt(b, getCode());
        writeString(b, getParams());
        writeBean(b, getData(), true);
    }

    @Override
    default int byteSize() {
        int size = 0;
        size += byteSize(getId());
        size += byteSize(getCode());
        size += byteSize(getParams());
        size += Byte.BYTES + (getData() == null ? 0 : getData().byteSize());
        return size;
    }
}
