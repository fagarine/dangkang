package cn.laoshini.dk.jit.generator;

import cn.laoshini.dk.jit.type.CompositeBean;

/**
 * 自定义格式消息（使用Netty的ByteBuf作为缓冲区）DTO类的生成器
 *
 * @author fagarine
 */
public class NettyCustomDtoClassFileGenerator extends AbstractCustomDtoClassFileGenerator {
    public NettyCustomDtoClassFileGenerator(CompositeBean compositeBean, ClassLoader classLoader) {
        super(compositeBean, classLoader, "io.netty.buffer.ByteBuf", "cn.laoshini.dk.server.message.INettyDto");
    }

    @Override
    public String candidateClassNamePrefix() {
        return "NettyCustomDto";
    }
}
