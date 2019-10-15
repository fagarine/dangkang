package cn.laoshini.dk.eventbus;

import com.google.common.eventbus.EventBus;

/**
 * 基于google EventBus的事件管理器
 *
 * @author fagarine
 */
public class EventMgr extends EventBus {

    private static EventMgr instance = new EventMgr();

    public static EventMgr getInstance() {
        return instance;
    }

    private EventMgr() {
        // guava EventBus的默认发布事件处理异常处理器LoggingSubscriberExceptionHandler没有记录异常堆栈，不能很好的定位问题
        super(new LoggingSubscriberExceptionHandler());
    }
}

