package cn.laoshini.dk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记类，被标记的类表示该类有方法会接收消息并处理，被标记的类一般称为Handler类
 * <p>
 * 系统默认会在注册Handler时，创建Handler的实例，如果该类中依赖了Spring组件，则Handler的实例会由Spring创建并管理
 * </p>
 *
 * @author fagarine
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageHandle {

    /**
     * {@link #id()}方法的名称
     */
    String ID_METHOD = "id";

    /**
     * 接收消息id，也是handler将会处理的协议号
     */
    int id();

    /**
     * 描述信息
     */
    String description() default "";

    /**
     * 是否允许玩家没有登录的情况下，发送该消息，默认为false <br>
     * 正常情况下，只会有有限的几条消息不用登录就发送，比如用户登录
     */
    boolean allowGuestRequest() default false;

    /**
     * 消息是否需要按到达顺序执行，默认为true，即需要按顺序执行<br>
     * 实现顺序执行的基本方法是把消息存入队列，然后从队列消费<br>
     * 正常情况下，只会有有限的几条消息不用按顺序执行，比如用户登录<br>
     * 注意：HTTP连接收到的消息可能会忽略此项，因为HTTP消息默认不会进入队列
     */
    boolean sequential() default true;
}
