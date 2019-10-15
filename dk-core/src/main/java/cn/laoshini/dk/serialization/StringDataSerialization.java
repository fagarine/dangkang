package cn.laoshini.dk.serialization;

import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSON;

import cn.laoshini.dk.util.TypeUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 字符串格式数据序列化功能<br>
 * 简单数据类型直接转为字符串处理，对象类型会先转成JSON字符串后处理
 *
 * @author fagarine
 */
public class StringDataSerialization implements IDataSerializable {

    private static final String JSON_TOKEN_BRACE = "{";
    private static final String JSON_TOKEN_BRACKET = "[";

    @Override
    public byte[] toBytes(Object value) {
        String str = null;
        if (value != null) {
            Class<?> originalType = value.getClass();
            // 简单数据类型直接转为字符串保存
            if (String.class.equals(originalType)) {
                str = (String) value;
            } else if (TypeUtil.isPrimitiveType(originalType)) {
                str = String.valueOf(value);
            } else {
                // 对象类型序列化成JSON字符串后保存
                str = JSON.toJSONString(value);
            }
        }
        return (str == null) ? EMPTY_BYTES : str.getBytes(UTF_8);
    }

    private String bytesToString(byte[] bytes) {
        return (bytes == null) ? null : new String(bytes, UTF_8);
    }

    @Override
    public Object toObject(byte[] bytes) {
        String value = bytesToString(bytes);
        if (value == null) {
            return null;
        }

        if (isJsonString(value)) {
            return JSON.parseObject(value);
        }
        return value;
    }

    private boolean isJsonString(String value) {
        return value.startsWith(JSON_TOKEN_BRACE) || value.startsWith(JSON_TOKEN_BRACKET);
    }

    @Override
    public <T> T toAssignedTypeObject(byte[] bytes, Class<T> toType) {
        String value = bytesToString(bytes);
        if (value == null || toType == null) {
            return null;
        }

        // 简单类型，直接转换
        if (String.class.equals(toType)) {
            return (T) value;
        } else if (TypeUtil.isPrimitiveType(toType)) {
            return (T) TypeUtil.parseBasicTypeString(value, toType);
        }

        // 对象类型按JSON字符串处理
        return JSON.parseObject(value, toType);
    }

    @Override
    public <T> List<T> toAssignedBeanList(byte[] bytes, Class<T> toType) {
        String value = bytesToString(bytes);
        if (value == null) {
            return Collections.emptyList();
        }

        return JSON.parseArray(value, toType);
    }
}
