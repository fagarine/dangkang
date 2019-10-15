package cn.laoshini.dk.net.codec;

import java.util.LinkedList;
import java.util.List;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.MessageLite;

import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.exception.MessageException;

/**
 * @author fagarine
 */
class ProtobufByteMessageDecoder implements IByteMessageDecoder<MessageLite> {

    private static final boolean HAS_PARSER;

    static {
        boolean hasParser = false;
        try {
            // MessageLite.getParserForType() is not available until protobuf 2.5.0.
            MessageLite.class.getDeclaredMethod("getParserForType");
            hasParser = true;
        } catch (Throwable t) {
            // Ignore
        }

        HAS_PARSER = hasParser;
    }

    private final MessageLite prototype;
    private final ExtensionRegistryLite extensionRegistry;

    ProtobufByteMessageDecoder(MessageLite prototype) {
        this(prototype, null);
    }

    ProtobufByteMessageDecoder(MessageLite prototype, ExtensionRegistry extensionRegistry) {
        if (prototype == null) {
            throw new NullPointerException("prototype不能为空");
        }
        this.prototype = prototype.getDefaultInstanceForType();
        this.extensionRegistry = extensionRegistry;
    }

    @Override
    public List<MessageLite> decode(byte[] bytes, int off, int len) {
        try {
            List<MessageLite> messages = new LinkedList<>();
            if (extensionRegistry == null) {
                if (HAS_PARSER) {
                    messages.add(prototype.getParserForType().parseFrom(bytes, off, len));
                } else {
                    messages.add(prototype.newBuilderForType().mergeFrom(bytes, off, len).build());
                }
            } else {
                if (HAS_PARSER) {
                    messages.add(prototype.getParserForType().parseFrom(bytes, off, len, extensionRegistry));
                } else {
                    messages.add(prototype.newBuilderForType().mergeFrom(bytes, off, len, extensionRegistry).build());
                }
            }
            return messages;
        } catch (Exception e) {
            throw new MessageException(GameCodeEnum.MESSAGE_DECODE_ERROR, "Protobuf消息解析出错", e);
        }
    }
}
