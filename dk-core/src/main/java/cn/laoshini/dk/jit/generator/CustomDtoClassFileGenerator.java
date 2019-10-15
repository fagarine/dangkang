package cn.laoshini.dk.jit.generator;

import java.nio.ByteBuffer;

import cn.laoshini.dk.jit.type.CompositeBean;
import cn.laoshini.dk.net.msg.ICustomDto;

/**
 * 自定义格式消息（使用JDK自带的{@link ByteBuffer}作为缓冲区）DTO类的生成器
 *
 * @author fagarine
 */
public class CustomDtoClassFileGenerator extends AbstractCustomDtoClassFileGenerator {

    public CustomDtoClassFileGenerator(CompositeBean compositeBean, ClassLoader classLoader) {
        super(compositeBean, classLoader, ByteBuffer.class.getName(), ICustomDto.class.getName());
    }

    @Override
    public String candidateClassNamePrefix() {
        return "CustomDto";
    }
}
