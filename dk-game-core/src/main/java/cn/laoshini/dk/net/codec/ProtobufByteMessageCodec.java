package cn.laoshini.dk.net.codec;

import java.util.Arrays;

import com.google.protobuf.InvalidProtocolBufferException;

import cn.laoshini.dk.constant.GameConstant;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.net.msg.BaseProtobufMessage;
import cn.laoshini.dk.util.ByteMessageUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * @author fagarine
 */
public class ProtobufByteMessageCodec implements IByteMessageCodec<BaseProtobufMessage.Base> {

    @Override
    public BaseProtobufMessage.Base decode(byte[] bytes, GameSubject subject) {
        if (bytes == null || bytes.length <= GameConstant.MESSAGE_LENGTH_OFFSET) {
            throw new BusinessException("receive.message.invalid",
                    String.format("消息长度非法:%s, subject:%s", Arrays.toString(bytes), subject));
        }

        int len = ByteMessageUtil.readMsgLength(bytes);
        if (len < 0 || len > GameConstant.MAX_FRAME_LENGTH) {
            throw new BusinessException("receive.message.invalid",
                    String.format("消息长度非法:%s, subject:%s", Arrays.toString(bytes), subject));
        }

        try {
            return BaseProtobufMessage.Base.newBuilder().mergeFrom(bytes, GameConstant.MESSAGE_LENGTH_OFFSET, len)
                    .build();
        } catch (InvalidProtocolBufferException e) {
            LogUtil.error("protobuf消息解析出错, gameId:{}, message:{}, subject:{}", subject.getGameId(),
                    Arrays.toString(bytes), subject);
            throw new BusinessException("message.decode.error", "protobuf消息解析出错");
        }
    }

    @Override
    public byte[] encode(BaseProtobufMessage.Base base, GameSubject subject) {
        return ByteMessageUtil.protobufToBytes(base);
    }
}
