package cn.laoshini.dk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于外置模块热更新，用来标记 <b>外置模块中</b>  <b>持有数据</b> 的 <b>单例</b> 、<b>枚举</b> 或 <b>静态数据类</b>
 * <p>
 * 被该注解标记的类，意为数据或内存资源的持有者，表示该类在热更新模块时，需要保留更新前的资源数据，并拷贝到更新后的对象中
 * </p>
 * <p>
 * 该注解用于模块热更新，所以外置模块需要使用该注解；当然，非外置模块也建议使用，这有助于使用表达式获取数据；<br>
 * 如果一个类已经被标记为Spring托管，则不需要再使用该注解标注，系统默认会保留Spring托管对象的数据；<br>
 * 如果一个类被{@link FunctionVariousWays}标记，也不需要使用该注解；<br>
 * 稍显特殊的，是被{@link MessageHandle}标记的类，如果Handler类中依赖了Spring组件，系统同样会使用Spring管理Handler对象；
 * </p>
 * 综上，该标记适用于非Spring托管，但是又长期持有数据的对象，使用该注解的类必须是 <b>单例</b> 、<b>枚举</b> 或 <b>静态数据类</b>；
 * 如外置模块中缓存了部分用户数据的manager类，在热更后需要保留热更前已缓存的数据，则需要添加该注解。
 *
 * @author fagarine
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceHolder {
    /**
     * 名称、描述，可选
     */
    String value() default "";
}
