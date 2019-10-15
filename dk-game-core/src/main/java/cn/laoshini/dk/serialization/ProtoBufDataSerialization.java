package cn.laoshini.dk.serialization;

import java.util.Collections;
import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;

import cn.laoshini.dk.net.msg.BaseProtobufMessage;
import cn.laoshini.dk.util.LogUtil;

/**
 * protobuf格式数据序列化功能<br>
 * 只处理BasePb.Base对象
 *
 * @author fagarine
 */
public class ProtoBufDataSerialization implements IDataSerializable {
    @Override
    public byte[] toBytes(Object object) {
        if (object instanceof BaseProtobufMessage.Base) {
            return ((BaseProtobufMessage.Base) object).toByteArray();
        }
        return EMPTY_BYTES;
    }

    @Override
    public BaseProtobufMessage.Base toObject(byte[] bytes) {
        try {
            return BaseProtobufMessage.Base.newBuilder().mergeFrom(bytes).build();
        } catch (InvalidProtocolBufferException e) {
            LogUtil.error("protobuf解析数据出错", e);
        }
        return null;
    }

    @Override
    public <T> T toAssignedTypeObject(byte[] bytes, Class<T> toType) {
        return null;
    }

    @Override
    public <T> List<T> toAssignedBeanList(byte[] bytes, Class<T> toType) {
        return Collections.emptyList();
    }

}
