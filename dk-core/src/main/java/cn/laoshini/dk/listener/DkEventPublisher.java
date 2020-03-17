package cn.laoshini.dk.listener;

import cn.laoshini.dk.common.SpringContextHolder;

/**
 * 当康系统事件发布者
 *
 * @author fagarine
 */
public class DkEventPublisher {
    private DkEventPublisher() {
    }

    /**
     * 发布事件
     *
     * @param event 事件对象
     */
    public static void publishEvent(DkContextEvent event) {
        SpringContextHolder.getContext().publishEvent(event);
    }

}
