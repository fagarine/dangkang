package cn.laoshini.dk.net.codec;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.constant.GameConstant;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.net.MessageDtoClassHolder;
import cn.laoshini.dk.net.msg.ICustomDto;
import cn.laoshini.dk.net.msg.ICustomMessage;
import cn.laoshini.dk.net.msg.ReqCustomMessage;
import cn.laoshini.dk.net.msg.ReqMessage;
import cn.laoshini.dk.util.ByteMessageUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * @author fagarine
 */
public class CustomByteMessageCodec implements IByteMessageCodec<ICustomMessage<? extends ICustomDto>> {

    /**
     * 自定义格式消息，消息体开始位置相对消息头的偏移位数
     */
    public static final int CUSTOM_MSG_OFFSET =
            GameConstant.MESSAGE_LENGTH_OFFSET + GameConstant.MESSAGE_CHECK_CODE_OFFSET
                    + GameConstant.MESSAGE_ID_OFFSET;

    /**
     * 缺省消息长度：1KB
     */
    public static final int DEFAULT_CAPACITY = 1024;

    /**
     * 最大允许消息长度：32M
     */
    public static final int MAX_CAPACITY = 1024 << 15;

    private CustomByteMessageCodec() {
    }

    private static CustomByteMessageCodec ins;

    public static CustomByteMessageCodec getInstance() {
        if (ins == null) {
            ins = new CustomByteMessageCodec();
        }
        return ins;
    }

    @Override
    public ICustomMessage<ICustomDto> decode(byte[] bytes, GameSubject subject) {
        if (bytes == null || bytes.length <= GameConstant.MESSAGE_LENGTH_OFFSET) {
            throw new BusinessException("receive.message.invalid",
                    String.format("消息长度非法:%s, subject:%s", Arrays.toString(bytes), subject));
        }

        int len = ByteMessageUtil.readMsgLength(bytes);
        if (len < 0 || len > GameConstant.MAX_FRAME_LENGTH) {
            throw new BusinessException("receive.message.invalid",
                    String.format("消息长度非法:%s, subject:%s", Arrays.toString(bytes), subject));
        }

        // 校验码检查逻辑
        int checkCode = ByteMessageUtil.readCheckCode(bytes);

        int msgId = ByteMessageUtil.readCustomMsgId(bytes);

        // 读取消息内容
        ByteBuffer buffer = ByteBuffer.wrap(bytes, CUSTOM_MSG_OFFSET, len);
        ReqCustomMessage<ICustomDto> reqMessage = new ReqCustomMessage<>();
        reqMessage.setDataType(MessageDtoClassHolder.getCustomDtoClass(msgId));
        reqMessage.read(buffer);
        return reqMessage;
    }

    @Override
    public byte[] encode(ICustomMessage<? extends ICustomDto> message, GameSubject subject) {
        ByteBuffer buffer = ByteBuffer.allocate(message.byteSize());

        boolean writeFlag = true;
        try {
            message.write(buffer);
        } catch (BufferOverflowException e) {
            LogUtil.error("消息id[{}]中的[{}]的byteSize()方法计算有问题，请检查", message.getId(), message.getData());

            // 自定义DTO的长度计算有问题，使用默认长度
            int capacity = DEFAULT_CAPACITY;
            while (writeFlag) {
                buffer = ByteBuffer.allocate(capacity);

                try {
                    message.write(buffer);
                    writeFlag = false;
                } catch (BufferOverflowException e1) {
                    // 如果长度还是不够，增大一倍
                    capacity = capacity << 1;

                    // 超过最大消息长度，抛出异常
                    if (capacity > MAX_CAPACITY) {
                        throw new MessageException(GameCodeEnum.MESSAGE_TOO_LARGE, "message.too.large",
                                "消息体过大，请拆分或检查逻辑:" + message);
                    }
                }
            }
        }

        buffer.flip();
        byte[] msgData = new byte[buffer.limit()];
        buffer.get(msgData);

        // 正常来讲，ReqMessage类型的消息不应该进入该方法，这里是做兼容处理，方便测试用
        int totalOffset = GameConstant.MESSAGE_LENGTH_OFFSET;
        if (message instanceof ReqMessage) {
            totalOffset = CustomByteMessageCodec.CUSTOM_MSG_OFFSET;
        }
        byte[] result = new byte[msgData.length + totalOffset];

        System.arraycopy(ByteMessageUtil.msgLengthToBytes(msgData.length), 0, result, 0,
                GameConstant.MESSAGE_LENGTH_OFFSET);

        if (message instanceof ReqMessage) {
            System.arraycopy(ByteMessageUtil.msgCheckCodeToBytes(0), 0, result, GameConstant.MESSAGE_LENGTH_OFFSET,
                    GameConstant.MESSAGE_CHECK_CODE_OFFSET);
            System.arraycopy(ByteMessageUtil.msgIdToBytes(message.getId()), 0, result,
                    GameConstant.MESSAGE_LENGTH_OFFSET + GameConstant.MESSAGE_CHECK_CODE_OFFSET,
                    GameConstant.MESSAGE_ID_OFFSET);
        }

        System.arraycopy(msgData, 0, result, totalOffset, msgData.length);
        return result;
    }
}
