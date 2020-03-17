package cn.laoshini.dk.listener;

import org.springframework.context.ApplicationListener;

/**
 * 当康系统事件监听器接口
 *
 * @author fagarine
 */
public interface IDkContextListener<E extends DkContextEvent> extends ApplicationListener<E> {

}
