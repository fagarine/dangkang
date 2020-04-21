package cn.laoshini.dk.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * 匹配条件：指定配置项的值，必须包含指定值
 *
 * @author fagarine
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnPropertyValueCondition.class)
public @interface ConditionalOnPropertyValue {

    /**
     * 配置项名称，全名，例如: dk.default-dao
     */
    String[] propertyName() default {};

    /**
     * 配置项的值中，必须包含的内容，仅在配置项的值不为空时有效。
     * <p>
     * 注意以下特殊情况：
     * 如果本方法返回空字符串，表示不管配置项是否存在，都匹配成功；
     * 如果在配置信息中找不到对应配置项的值，则默认表示匹配成功
     * </p>
     */
    String havingValue() default "";

    /**
     * 配置项不存在时，是否匹配；如果本方法返回true，当配置项不存在时，则匹配不通过，否则匹配通过
     */
    boolean matchIfMissing() default false;
}
