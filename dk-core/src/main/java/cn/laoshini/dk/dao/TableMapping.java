package cn.laoshini.dk.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.common.base.CaseFormat;

/**
 * 用于标记类，表示该类对应一个数据库表
 * <p>
 * 使用该注解，并期望它能正常工作，需要遵守以下约定：
 * </p>
 * <ul>
 * <li>被该注解标记的类一定是一个与数据库表对应的实体类</li>
 * <li>实体类中应该是一个POJO类，尽量不要在实体类中添加逻辑</li>
 * <li>使用该注解标注的类，类名可以不与表名一致，但是必须用{@link #value()}指定表名</li>
 * <li>类中的所有字段名称必须与数据库中表字段的名称一致，支持驼峰命名与下划线命名等命名法的装换，具体支持类型参见：{@link CaseFormat}</li>
 * <li>该注解必须与系统提供的内建DAO功能联合使用，具体参见：{@link cn.laoshini.dk.dao}</li>
 * <li>与表中的主键对应的字段，应该用@{@link TableKey}标记</li>
 * </ul>
 *
 * @author fagarine
 * @see TableKey
 * @see cn.laoshini.dk.dao.IRelationalDbDao
 * @see cn.laoshini.dk.dao.IRelationalDbDaoManager
 * @see cn.laoshini.dk.manager.EntityClassManager
 * @see cn.laoshini.dk.dao.IDefaultDao
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableMapping {

    /**
     * 唯一标识符，表名，如果该值为空，默认使用类名作为表名
     */
    String value() default "";

    /**
     * 表中字段的命名规则，默认为下划线小写命名法
     */
    CaseFormat columnFormat() default CaseFormat.LOWER_UNDERSCORE;

    /**
     * 类中字段的命名规则，默认为小驼峰命名法
     */
    CaseFormat fieldFormat() default CaseFormat.LOWER_CAMEL;

    /**
     * 名称或描述信息
     */
    String description() default "";
}
