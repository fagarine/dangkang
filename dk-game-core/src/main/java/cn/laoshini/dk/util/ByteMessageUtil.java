package cn.laoshini.dk.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.constant.GameConstant;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.function.VariousWaysManager;
import cn.laoshini.dk.net.IMessageHandlerManager;
import cn.laoshini.dk.net.msg.BaseProtobufMessage;
import cn.laoshini.dk.net.msg.IByteDto;
import cn.laoshini.dk.net.msg.ReqMessage;

/**
 * 字节数组格式的消息辅助工作类
 *
 * @author fagarine
 */
public class ByteMessageUtil {
    private ByteMessageUtil() {
    }

    public static void main(String[] args) {
        System.out.println(IByteDto.SUPPORTED_TYPES);
    }

    public static int bytesToInt(byte[] bytes, int startIndex, int length) {
        int result = 0;
        for (int i = startIndex; i < startIndex + length; i++) {
            result = ((bytes[i] & 0xff) | (result << Byte.SIZE));
        }
        return result;
    }

    /**
     * 读取字节数组内容，并计算作为消息的长度返回，数组读取长度由常量{@link GameConstant#MESSAGE_LENGTH_OFFSET}决定
     *
     * @param bytes 字节数组
     * @return 该方法不会返回null
     */
    public static int readMsgLength(byte[] bytes) {
        return bytesToInt(bytes, 0, GameConstant.MESSAGE_LENGTH_OFFSET);
    }

    /**
     * 读取字节数组内容，并计算作为消息的校验码返回，数组读取长度由常量{@link GameConstant#MESSAGE_CHECK_CODE_OFFSET}决定
     *
     * @param bytes 字节数组
     * @return 该方法不会返回null
     */
    public static int readCheckCode(byte[] bytes) {
        return bytesToInt(bytes, GameConstant.MESSAGE_LENGTH_OFFSET, GameConstant.MESSAGE_CHECK_CODE_OFFSET);
    }

    /**
     * 读取字节数组内容，并计算作为自定义消息的消息id返回，数组读取长度由常量{@link GameConstant#MESSAGE_ID_OFFSET}决定
     *
     * @param bytes 字节数组
     * @return 该方法不会返回null
     */
    public static int readCustomMsgId(byte[] bytes) {
        return bytesToInt(bytes, GameConstant.MESSAGE_LENGTH_OFFSET + GameConstant.MESSAGE_CHECK_CODE_OFFSET,
                GameConstant.MESSAGE_ID_OFFSET);
    }

    public static byte[] intToBytes(int value, int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[length - 1 - i] = (byte) (value >> Byte.SIZE * i);
        }
        return bytes;
    }

    /**
     * 将长度转换为字节数组返回，数组长度由常量{@link GameConstant#MESSAGE_LENGTH_OFFSET}决定
     *
     * @param len 消息长度
     * @return 该方法不会返回nul
     */
    public static byte[] msgLengthToBytes(int len) {
        return intToBytes(len, GameConstant.MESSAGE_LENGTH_OFFSET);
    }

    /**
     * 将校验码转换为字节数组返回，数组长度由常量{@link GameConstant#MESSAGE_CHECK_CODE_OFFSET}决定
     *
     * @param checkCode 校验码
     * @return 该方法不会返回nul
     */
    public static byte[] msgCheckCodeToBytes(int checkCode) {
        return intToBytes(checkCode, GameConstant.MESSAGE_CHECK_CODE_OFFSET);
    }

    public static byte[] msgIdToBytes(int msgId) {
        return intToBytes(msgId, GameConstant.MESSAGE_ID_OFFSET);
    }

    /**
     * {@link BaseProtobufMessage.Base}对象转为{@link ReqMessage}对象
     *
     * @param msg 消息内容
     * @return 该方法不会返回null
     */
    public static ReqMessage<Message> baseToReqMessage(BaseProtobufMessage.Base msg) {
        ReqMessage<Message> reqMessage = new ReqMessage<>();
        reqMessage.setId(msg.getCmd());
        reqMessage.setParams(msg.getParams());

        Class<Message> type = VariousWaysManager.getCurrentImpl(IMessageHandlerManager.class)
                .getProtobufHandlerGenericType(msg.getCmd());
        try {
            if (type != null) {
                reqMessage.setData(msg.getDetail().unpack(type));
            }
        } catch (InvalidProtocolBufferException e) {
            throw new MessageException(GameCodeEnum.PARAM_ERROR, "any.unpack.error", "解析protobuf消息的detail数据出错:" + msg);
        }
        return reqMessage;
    }

    /**
     * 将protobuf消息编码成byte数组（数组前记录消息长度）
     *
     * @param msg 消息体
     * @return 返回byte数组
     */
    public static byte[] protobufToBytes(BaseProtobufMessage.Base msg) {
        byte[] msgData = msg.toByteArray();
        byte[] lenBytes = msgLengthToBytes(msgData.length);
        byte[] result = new byte[msgData.length + GameConstant.MESSAGE_LENGTH_OFFSET];
        System.arraycopy(lenBytes, 0, result, 0, GameConstant.MESSAGE_LENGTH_OFFSET);
        System.arraycopy(msgData, 0, result, GameConstant.MESSAGE_LENGTH_OFFSET, msgData.length);
        return result;
    }

}
