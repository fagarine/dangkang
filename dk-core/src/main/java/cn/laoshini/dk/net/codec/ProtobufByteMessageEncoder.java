package cn.laoshini.dk.net.codec;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;

/**
 * @author fagarine
 */
class ProtobufByteMessageEncoder implements IByteMessageEncoder<MessageLiteOrBuilder> {

    @Override
    public byte[] encode(MessageLiteOrBuilder message) {
        if (message instanceof MessageLite) {
            return ((MessageLite) message).toByteArray();
        }
        if (message instanceof MessageLite.Builder) {
            return ((MessageLite.Builder) message).build().toByteArray();
        }
        return null;
    }
}
