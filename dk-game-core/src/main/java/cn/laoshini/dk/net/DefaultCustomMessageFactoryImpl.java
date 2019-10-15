package cn.laoshini.dk.net;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.net.msg.ICustomMessage;

/**
 * 自定义消息工厂实现类
 *
 * @author fagarine
 */
@Component
@FunctionVariousWays
public class DefaultCustomMessageFactoryImpl implements ICustomMessageFactory<ICustomMessage> {
    private DefaultCustomMessageFactoryImpl() {
    }

    private final Map<Integer, Class<ICustomMessage>> messages = new HashMap<>();

    private final Map<Integer, Class<ICustomMessage>> messageCache = new HashMap<>();

    @Override
    public void registerMessage(int messageId, Class<ICustomMessage> messageClass) {
        messages.put(messageId, messageClass);
    }

    @Override
    public void prepareBatchUnregister(Collection<Integer> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        for (Integer messageId : messageIds) {
            messageCache.put(messageId, messages.remove(messageId));
        }
    }

    @Override
    public void unregisterMessages(Collection<Integer> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        for (Integer messageId : messageIds) {
            messageCache.remove(messageId);
        }
    }

    @Override
    public Class<ICustomMessage> getMessageClass(int messageId) {
        Class<ICustomMessage> messageClass = messages.get(messageId);
        if (messageClass == null) {
            messageClass = messageCache.get(messageId);
        }
        return messageClass;
    }

}
