package cn.laoshini.dk.eventbus;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

import cn.laoshini.dk.util.LogUtil;

/**
 * guava EventBus的默认发布事件处理异常处理器
 * 没有记录异常堆栈，不能很好的定位问题
 *
 * @author fagarine
 */
public class LoggingSubscriberExceptionHandler implements SubscriberExceptionHandler {

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {
        LogUtil.error("Could not dispatch event: " + context.getSubscriber() + " to " + context.getSubscriberMethod(),
                exception);
    }

}
