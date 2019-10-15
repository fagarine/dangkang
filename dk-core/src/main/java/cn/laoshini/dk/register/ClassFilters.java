package cn.laoshini.dk.register;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

/**
 * @author fagarine
 */
public class ClassFilters {
    private ClassFilters() {
    }

    public static IClassFilter equalTo(Class<?> compareClass) {
        return clazz -> clazz.equals(compareClass);
    }

    /**
     * 返回一个以指定注解为条件的类过滤器
     *
     * @param annotationClass 注解类
     * @return 返回过滤器
     */
    public static IClassFilter newAnnotationFilter(final Class<? extends Annotation> annotationClass) {
        return (clazz) -> clazz.isAnnotationPresent(annotationClass);
    }

    /**
     * 返回一个以指定超类为条件的类过滤器
     *
     * @param parentClass 指定超类（父类或接口类）
     * @param abstraction 是否允许抽象类通过过滤
     * @param innerClass 是否允许内部类通过过滤
     * @return 返回过滤器
     */
    public static IClassFilter newParentClassFilter(final Class<?> parentClass, final boolean abstraction,
            final boolean innerClass) {
        return clazz -> {
            if (clazz.equals(parentClass) || parentClass.isAssignableFrom(clazz)) {
                if (abstraction || !Modifier.isAbstract(clazz.getModifiers())) {
                    return innerClass || !clazz.getName().contains("$");
                }
            }
            return false;
        };
    }

    /**
     * 返回一个以指定超类为条件的类过滤器（包含抽象类和内部类）
     *
     * @param parentClass 指定超类（父类或接口类）
     * @return 返回过滤器
     */
    public static IClassFilter newParentClassFilter(final Class<?> parentClass) {
        return newParentClassFilter(parentClass, true, true);
    }

    /**
     * 返回一个以指定注解类和指定超类为条件的类过滤器（类必须被指定注解标记，并且是指定类的子类或实现类，才算符合条件）
     *
     * @param annotationClass 注解类
     * @param parentClass 指定超类（父类或接口类）
     * @param abstraction 是否允许抽象类通过过滤
     * @param innerClass 是否允许内部类通过过滤
     * @return 返回过滤器
     */
    public static IClassFilter newAnnotationAndParentClassFilter(final Class<? extends Annotation> annotationClass,
            final Class<?> parentClass, final boolean abstraction, final boolean innerClass) {
        return clazz -> {
            if (clazz.isAnnotationPresent(annotationClass) && (clazz.equals(parentClass) || parentClass
                    .isAssignableFrom(clazz))) {
                if (abstraction || !Modifier.isAbstract(clazz.getModifiers())) {
                    return innerClass || !clazz.getName().contains("$");
                }
            }
            return false;
        };
    }

    /**
     * 返回一个以指定注解类和指定超类为条件的类过滤器（类必须被指定注解标记，并且是指定类的子类或实现类，才算符合条件，包含抽象类和内部类）
     *
     * @param annotationClass 注解类
     * @param parentClass 指定超类（父类或接口类）
     * @return 返回过滤器
     */
    public static IClassFilter newAnnotationAndParentClassFilter(final Class<? extends Annotation> annotationClass,
            final Class<?> parentClass) {
        return newAnnotationAndParentClassFilter(annotationClass, parentClass, true, true);
    }

    /**
     * 返回一个以指定注解类或指定超类为条件的类过滤器（类被指定注解标记，或者是指定类的子类或实现类，都算符合条件）
     *
     * @param annotationClass 注解类
     * @param parentClass 指定超类（父类或接口类）
     * @param abstraction 是否允许抽象类通过过滤
     * @param innerClass 是否允许内部类通过过滤
     * @return 返回过滤器
     */
    public static IClassFilter newAnnotationOrParentClassFilter(final Class<? extends Annotation> annotationClass,
            final Class<?> parentClass, final boolean abstraction, final boolean innerClass) {
        return clazz -> {
            if (clazz.equals(parentClass) || clazz.isAnnotationPresent(annotationClass) || parentClass
                    .isAssignableFrom(clazz)) {
                if (abstraction || !Modifier.isAbstract(clazz.getModifiers())) {
                    return innerClass || !clazz.getName().contains("$");
                }
            }
            return false;
        };
    }

    /**
     * 返回一个以指定注解类或指定超类为条件的类过滤器（类被指定注解标记，或者是指定类的子类或实现类，都算符合条件，包含抽象类和内部类）
     *
     * @param annotationClass 注解类
     * @param parentClass 指定超类（父类或接口类）
     * @return 返回过滤器
     */
    public static IClassFilter newAnnotationOrParentClassFilter(final Class<? extends Annotation> annotationClass,
            final Class<?> parentClass) {
        return newAnnotationOrParentClassFilter(annotationClass, parentClass, true, true);
    }
}
