package cn.laoshini.dk.listener;

import org.springframework.context.event.ApplicationContextEvent;

import cn.laoshini.dk.common.SpringContextHolder;

/**
 * @author fagarine
 */
public class DkContextEvent extends ApplicationContextEvent {

    public DkContextEvent() {
        super(SpringContextHolder.getContext());
    }
}
