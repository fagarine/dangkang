package cn.laoshini.dk.net.msg;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被标记的类表示该类为一个自定义消息类型，仅用于自定义消息类型
 *
 * @author fagarine
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomMsg {

    /**
     * 消息id
     */
    int id();

    /**
     * 方法或功能描述信息
     */
    String value() default "";
}
