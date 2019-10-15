package cn.laoshini.dk.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记类变量，表示该字段为主键，
 * <p>
 * 注意：使用该注解，必须保证该类已被@{@link TableMapping}标记，否则单独使用无效<br>
 * 另外：如果一个类中有多个字段被标记，则会被视为联合主键看待
 * </p>
 *
 * @author fagarine
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableKey {

    /**
     * 描述信息，不可用于具体的业务逻辑
     */
    String value() default "";

}
