package cn.laoshini.dk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.laoshini.dk.constant.Constants;

/**
 * 可配置功能依赖注解，用来标记实例变量，使用本注解标记的Field，表示该变量指向一个可配置功能的实现对象，系统会完成该变量依赖的自动注入
 * <p>
 * 使用该注解，需要遵从以下约定：
 * </P>
 * <ul>
 * <li>本注解只应该能用来标记实例变量，推荐使用在被Spring托管的组件类中</li>
 * <li>被标记的变量的声明类型，必须是某个可配置功能的定义类（即被注解 @{@link ConfigurableFunction} 标记的类），而不是其实现类</li>
 * <li>用户可以通过{@link #value()}指定使用哪一个实现</li>
 * <li>如果变量指向的功能实现允许为空（即被本注解标记的实例变量本身允许为空），需要在标记时显示的将 {@link #nullable()} 设置为true</li>
 * </ul>
 *
 * @author fagarine
 * @see ConfigurableFunction
 * @see FunctionVariousWays
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FunctionDependent {

    /**
     * 指定选择使用哪一种实现方式，对应{@link FunctionVariousWays#value()}，不区分大小写
     * <p>
     * 系统默认值为空字符串，该值指向的是被依赖功能的当前默认实现，
     * 而非基本实现（基本实现是指实现key为{@link Constants#DEFAULT_PROPERTY_NAME}的实现），
     * 如果用户明确需要使用功能的某种实现或者基本实现对象，则需要通过设置该值，将依赖指向对应的实现。
     * <p>
     * 特别的，如果系统中发现了被依赖功能的多种实现，用户却没有通过配置项选择使用哪一种作为当前的默认实现，
     * 则系统会在这些实现中随机选择一种作为当前默认实现，
     * 而用户如果又没有设置该值（即指向当前默认实现），则最终指向的就是系统随机到的这种实现，这是不可靠的（外置模块的插拔可能导致当前默认实现改变成其他值）。
     * </p>
     */
    String value() default "";

    /**
     * 被本注解标注的变量，所指向的实现对象，是否允许为空；如果不允许（默认为不允许），当找不到对应的实现时，将会抛出异常
     */
    boolean nullable() default false;

    /**
     * 如果希望依赖对象注入完成后，立即执行指定逻辑，可以在这里记录要执行的方法名（注意：该方法必须是被标记的类中的 <b>无参方法</b> ）
     * <p>
     * 特别的：如果本注解被用于标记static变量，则initMethod也必须指向static方法
     * </p>
     *
     * @return 返回依赖注入完成后需要执行的方法名
     */
    String initMethod() default "";

    /**
     * 描述信息
     */
    String description() default "";

}
