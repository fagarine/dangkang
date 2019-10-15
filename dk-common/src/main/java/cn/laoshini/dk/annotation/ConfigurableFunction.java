package cn.laoshini.dk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可配置功能标记，使用该注解标记的类，表示是一个功能定义类，其允许有多种不同实现，用户可以通过添加功能实现类的项目依赖和配置指定，来选择具体使用哪种实现。
 * <p>
 * 功能通过配置的好处：
 * <ul>
 * <li>将功能与其他依赖的功能解耦，功能之间通过接口调用，不需要知道具体实现，甚至不需要关心实现类是否是单例</li>
 * <li>方便功能的统一管理，实现类的实例对象由系统统一管理</li>
 * <li>实现依赖最小化，可以针对某个功能实现单独做一个项目，用户只需要单独加入这个项目的依赖即可，避免其他代码干扰</li>
 * </ul>
 * </p>
 * <p>
 * 注意：本注解需要与@{@link FunctionVariousWays}注解配合使用，
 * 本注解用来标记功能定义类（一般为接口），而{@link FunctionVariousWays}用来标记功能的实现类。<br>
 * 另外：功能定义类在项目启动时，应该作为classpath的一部分，被系统类加载器加载，而不应该放在可插拔模块中。
 * </p>
 *
 * @author fagarine
 * @see FunctionVariousWays
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurableFunction {

    /**
     * 该功能在配置项中的key，用户通过这个值作为配置项的key来选择实现方式（大小写敏感）
     */
    String key();

    /**
     * 功能说明
     */
    String description() default "";
}
