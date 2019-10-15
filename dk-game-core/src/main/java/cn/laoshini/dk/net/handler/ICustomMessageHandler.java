package cn.laoshini.dk.net.handler;

import cn.laoshini.dk.net.msg.ICustomDto;

/**
 * 自定义消息类型的消息处理handler，专为处理自定义消息设计
 *
 * @author fagarine
 */
public interface ICustomMessageHandler<DataType extends ICustomDto> extends IMessageHandler<DataType> {
}
