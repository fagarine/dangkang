package cn.laoshini.dk.config.center.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记配置信息表配置项的注解，使用该注解表示被标记字段数据需要保存到配置信息表中
 * <p>
 * 该注解为使用spring cloud config功能而设计，cloud config配置源（一般为配置文件，该项目中使用数据库保存配置项）获取规则如下：
 * <pre>
 * /{application}/{profile}[/{label}]
 * /{application}-{profile}.yml
 * /{label}/{application}-{profile}.yml
 * /{application}-{profile}.properties
 * /{label}/{application}-{profile}.properties
 * </pre>
 * <p>
 * 以上方式的url都指向同一配置源，即通过application、profile、label三个值来区分和指向一个配置源（以键值对方式记录的多个配置项），
 * 系统默认的profile值为“default”，label为“master”
 * </p>
 * <p>
 * 使用该注解来标注一个字段时，表示使用该字段的值作为该类中其他字段配置项的profile
 * </p>
 * 该注解已经包含了@{@link Property}功能，如果使用该注解标注的字段不需要额外指定名称，则不再需要显式添加@{@link Property}注解
 *
 * @author fagarine
 * @see Property
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Profile {

    /**
     * 配置项的名称（key），该值可以不设置，不设置表示使用被标记字段本身的名称作为配置项的key
     */
    String value() default "";
}
