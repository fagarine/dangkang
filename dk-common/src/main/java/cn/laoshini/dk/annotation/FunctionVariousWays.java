package cn.laoshini.dk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.laoshini.dk.constant.Constants;

/**
 * 功能多样性实现标记，用于标记某个功能的实现类
 * <p>
 * 表示某个功能可能具有多种实现，被标记的类就是其中一种实现，并且用户可以通过配置项来选择使用哪种实现方式（如果使用缺省实现，不需要在配置项中配置）；<br>
 * 所谓多种实现，意味着起码有一个基本的实现方式作为缺省（当然也可以没有，主要看依赖方依赖于哪种实现方式），因为某种原因不能满足需求，或不适合当前场景，所以选择更适合的实现方式
 * </p>
 * <p>
 * 注意：本注解需要与@{@link ConfigurableFunction}注解配合使用，ConfigurableFunction用来标记功能定义类，而本注解则用来标记实现类
 * </p>
 *
 * @author fagarine
 * @see ConfigurableFunction
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FunctionVariousWays {

    /**
     * 功能名称，作为区分其他实现的key（不区分大小写），用于用户选择实现方式；不指定则默认为是基本实现（key为{@link Constants#DEFAULT_PROPERTY_NAME}）
     */
    String value() default Constants.DEFAULT_PROPERTY_NAME;

    /**
     * 实现类的实例是否是单例
     * <p>
     * 如果是单例对象，会把实例对象交给Spring容器管理；如果不是，则会在每次获取实现对象时，新生成实现对象
     * </p>
     *
     * @return 是否是单例对象
     */
    boolean singleton() default true;

    /**
     * 功能说明
     */
    String description() default "";
}
