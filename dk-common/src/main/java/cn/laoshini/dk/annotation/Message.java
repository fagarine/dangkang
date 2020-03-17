package cn.laoshini.dk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记类，被标记的类表示该类是一个消息类
 *
 * @author fagarine
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Message {

    /**
     * 消息id
     */
    int id();

    /**
     * 描述信息
     */
    String desc() default "";

    /**
     * 是否是GM消息
     */
    boolean gm() default false;
}
