package cn.laoshini.dk.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.constant.GameConstant;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.net.MessageHolder;
import cn.laoshini.dk.net.msg.AbstractMessage;
import cn.laoshini.dk.net.msg.ReqMessage;
import cn.laoshini.dk.net.msg.RespMessage;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 游戏消息相关工具类
 *
 * @author fagarine
 */
public class MessageUtil {
    private MessageUtil() {
    }

    private static Integer checkMessageId(JSONObject object) {
        if (!object.containsKey(GameConstant.MESSAGE_ID_KEY)) {
            throw new MessageException(GameCodeEnum.MESSAGE_ID_NULL, "message.id.null", "消息ID不能为空");
        }
        return object.getInteger(GameConstant.MESSAGE_ID_KEY);
    }

    /**
     * 将JSONObject格式的消息，转换为AbstractMessage消息对象
     *
     * @param object JSONObject格式的消息对象
     * @return 该方法不会返回null，但可能抛出MessageException异常
     */
    public static AbstractMessage<Object> jsonObjectToMessage(JSONObject object) {
        int messageId = checkMessageId(object);
        AbstractMessage<Object> message;
        if (object.containsKey(GameConstant.MESSAGE_CODE_KEY)) {
            message = new RespMessage<>();
            message.setCode(object.getInteger(GameConstant.MESSAGE_CODE_KEY));
        } else {
            message = new ReqMessage<>();
        }
        message.setId(messageId);
        message.setParams(object.getString(GameConstant.MESSAGE_EXTENDS_KEY));

        if (GameCodeEnum.OK.getCode() == message.getCode()) {
            Class<?> clazz = MessageHolder.getMessageClass(messageId);
            JSONObject detail = object.getJSONObject(GameConstant.MESSAGE_DETAIL_KEY);
            if (clazz != null) {
                message.setData(detail.toJavaObject(clazz));
            } else {
                message.setData(detail);
            }
        }

        return message;
    }

    public static List<AbstractMessage<?>> jsonStrToMessage(String jsonStr) {
        List<AbstractMessage<?>> messages = new LinkedList<>();
        Object object = JSON.parse(jsonStr);
        if (object instanceof JSONObject) {
            messages.add(jsonObjectToMessage((JSONObject) object));
        } else if (object instanceof JSONArray) {
            JSONArray array = (JSONArray) object;
            for (Object o : array) {
                if (o instanceof JSONObject) {
                    messages.add(jsonObjectToMessage((JSONObject) o));
                }
            }
        }

        return messages;
    }

    public static List<AbstractMessage<?>> jsonBytesToMessage(byte[] bytes) {
        String jsonStr = new String(bytes, UTF_8);
        return jsonStrToMessage(jsonStr);
    }

    public static byte[] messageToJsonBytes(AbstractMessage<?> message) {
        String jsonStr = messageToJsonString(message);
        return jsonStr.getBytes(UTF_8);
    }

    public static String messageToJsonString(AbstractMessage<?> message) {
        Map<String, Object> map = new HashMap<>(4);
        map.put(GameConstant.MESSAGE_ID_KEY, message.getId());
        if (message instanceof RespMessage) {
            map.put(GameConstant.MESSAGE_CODE_KEY, message.getCode());
        }
        map.put(GameConstant.MESSAGE_DETAIL_KEY, message.getData());
        map.put(GameConstant.MESSAGE_EXTENDS_KEY, message.getParams());

        return JSON.toJSONString(map);
    }
}
