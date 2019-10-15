package cn.laoshini.dk.net.handler;

import com.google.protobuf.Message;

/**
 * protobuf消息处理handler，专为处理protobuf消息设计
 *
 * @author fagarine
 */
public interface IProtobufMessageHandler<AnyType extends Message> extends IMessageHandler<AnyType> {

}
