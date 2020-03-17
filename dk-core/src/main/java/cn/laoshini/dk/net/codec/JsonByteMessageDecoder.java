package cn.laoshini.dk.net.codec;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.laoshini.dk.domain.msg.AbstractMessage;
import cn.laoshini.dk.util.MessageUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * JSON格式消息解码器
 *
 * @author fagarine
 */
public class JsonByteMessageDecoder implements IByteMessageDecoder<AbstractMessage<?>> {

    @Override
    public List<AbstractMessage<?>> decode(byte[] bytes, int off, int len) {
        byte[] content = Arrays.copyOfRange(bytes, off, off + len);
        List<AbstractMessage<?>> messages = new LinkedList<>();
        String jsonStr = new String(content, UTF_8);
        Object object = JSON.parse(jsonStr);
        if (object instanceof JSONObject) {
            messages.add(buildReqMessage((JSONObject) object));
        } else if (object instanceof JSONArray) {
            JSONArray array = (JSONArray) object;
            for (Object o : array) {
                if (o instanceof JSONObject) {
                    messages.add(buildReqMessage((JSONObject) o));
                }
            }
        }

        return messages;
    }

    /**
     * 解码单条消息数据，并将消息内容转换为ReqMessage对象返回（未对消息内容进行解密相关处理，如果有需要，可重写该方法）
     *
     * @param object JSONObject方式记录的消息
     * @return 返回消息解码结果
     */
    protected AbstractMessage<?> buildReqMessage(JSONObject object) {
        return MessageUtil.jsonObjectToMessage(object);
    }
}
