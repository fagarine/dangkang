package cn.laoshini.dk.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * 匹配条件：指定配置项必须 不存在；仅当指定的所有配置项不存在的情况下，条件成立
 *
 * @author fagarine
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnPropertyMissingCondition.class)
public @interface ConditionalOnPropertyMissing {

    /**
     * 当条件只受单个配置项约束，或者有多个配置项但前缀名称不一致时，建议使用该方法；
     * 注意：使用该方法必须填写配置项的全名
     * 如果使用了该方法，还使用了{@link #prefix()}和{@link #name()}，则会取他们的并集做匹配
     */
    String[] value() default {};

    /**
     * 如果有多个配置项，且具有同样的前缀名，建议使用该方法加{@link #name()}的形式，该方法需要与{@link #name()}联合使用
     */
    String prefix() default "";

    /**
     * 配置项的名称，该方法需要与{@link #prefix()}联合使用，prefix + "." + name 视为一个完整的配置项名称
     */
    String[] name() default {};

}
