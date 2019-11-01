package cn.laoshini.dk.register;

import java.lang.annotation.Annotation;

/**
 * @author fagarine
 */
public class ClassScanners {
    private ClassScanners() {
    }

    /**
     * 创建并返回一个查找指定包路径（递归查找子包）下所有类的类扫描器
     *
     * @param packagePrefixes 包路径前缀名
     * @return 返回系统默认实现的类扫描器
     */
    public static <R> DefaultClassScanner<R> newPackageScanner(String[] packagePrefixes) {
        return new DefaultClassScanner<R>().setPackagePrefixes(packagePrefixes);
    }

    /**
     * 创建并返回一个查找指定包路径（递归查找子包）下，符合指定类过滤条件的所有类的类扫描器
     *
     * @param classFilter 类过滤条件
     * @param packagePrefixes 包路径前缀名
     * @return 返回系统默认实现的类扫描器
     */
    public static <R> DefaultClassScanner<R> newPackageScanner(IClassFilter classFilter, String[] packagePrefixes) {
        return new DefaultClassScanner<R>().setClassFilter(classFilter).setPackagePrefixes(packagePrefixes);
    }

    /**
     * 创建并返回一个通过指定超类扫描其子类的类扫描器
     *
     * @param parentClass 超类（父类或接口类）
     * @return 返回系统默认实现的类扫描器
     */
    public static <R> DefaultClassScanner<R> newParentScanner(Class<?> parentClass) {
        return new DefaultClassScanner<R>().setClassFilter(ClassFilters.newParentClassFilter(parentClass));
    }

    public static <R> DefaultClassScanner<R> newParentScanner(Class<?> parentClass, boolean abstraction,
            boolean innerClass) {
        return new DefaultClassScanner<R>()
                .setClassFilter(ClassFilters.newParentClassFilter(parentClass, abstraction, innerClass));
    }

    public static <R> DefaultClassScanner<R> newParentScanner(Class<?> parentClass, String[] packagePrefixes) {
        return new DefaultClassScanner<R>().setClassFilter(ClassFilters.newParentClassFilter(parentClass))
                .setPackagePrefixes(packagePrefixes);
    }

    public static <R> DefaultClassScanner<R> newParentScanner(Class<?> parentClass, String[] packagePrefixes,
            boolean abstraction, boolean innerClass) {
        return new DefaultClassScanner<R>()
                .setClassFilter(ClassFilters.newParentClassFilter(parentClass, abstraction, innerClass))
                .setPackagePrefixes(packagePrefixes);
    }

    /**
     * 创建并返回一个通过指定注解扫描类的类扫描器
     *
     * @param annotationClass 注解类
     * @return 返回系统默认实现的类扫描器
     */
    public static <R> DefaultClassScanner<R> newAnnotationScanner(Class<? extends Annotation> annotationClass) {
        return new DefaultClassScanner<R>().setClassFilter(ClassFilters.newAnnotationFilter(annotationClass));
    }

    public static <R> DefaultClassScanner<R> newAnnotationScanner(Class<? extends Annotation> annotationClass,
            String[] packagePrefixes) {
        return new DefaultClassScanner<R>().setClassFilter(ClassFilters.newAnnotationFilter(annotationClass))
                .setPackagePrefixes(packagePrefixes);
    }

    /**
     * 创建并返回一个通过 指定超类 <b>和</b> 指定注解 扫描类的类扫描器
     * 注意：只有在一个类既是指定类的子类，且被指定的注解标记，才算符合条件
     *
     * @param annotationClass 注解类
     * @param parentClass 超类（父类或接口类）
     * @return 返回系统默认实现的类扫描器
     */
    public static <R> DefaultClassScanner<R> newAnnotationAndParentScanner(Class<? extends Annotation> annotationClass,
            Class<?> parentClass) {
        return new DefaultClassScanner<R>()
                .setClassFilter(ClassFilters.newAnnotationAndParentClassFilter(annotationClass, parentClass));
    }

    public static <R> DefaultClassScanner<R> newAnnotationAndParentScanner(Class<? extends Annotation> annotationClass,
            Class<?> parentClass, boolean abstraction, boolean innerClass) {
        return new DefaultClassScanner<R>().setClassFilter(
                ClassFilters.newAnnotationAndParentClassFilter(annotationClass, parentClass, abstraction, innerClass));
    }

    public static <R> DefaultClassScanner<R> newAnnotationAndParentScanner(Class<? extends Annotation> annotationClass,
            Class<?> parentClass, String[] packagePrefixes) {
        return new DefaultClassScanner<R>()
                .setClassFilter(ClassFilters.newAnnotationAndParentClassFilter(annotationClass, parentClass))
                .setPackagePrefixes(packagePrefixes);
    }

    public static <R> DefaultClassScanner<R> newAnnotationAndParentScanner(Class<? extends Annotation> annotationClass,
            Class<?> parentClass, boolean abstraction, boolean innerClass, String[] packagePrefixes) {
        return new DefaultClassScanner<R>().setClassFilter(
                ClassFilters.newAnnotationAndParentClassFilter(annotationClass, parentClass, abstraction, innerClass))
                .setPackagePrefixes(packagePrefixes);
    }

    /**
     * 创建并返回一个通过 指定超类 <b>或</b> 指定注解 扫描类的类扫描器
     * 注意：只要一个类是指定类的子类，或被指定的注解标记，就算符合条件
     *
     * @param annotationClass 注解类
     * @param parentClass 超类（父类或接口类）
     * @return 返回系统默认实现的类扫描器
     */
    public static <R> DefaultClassScanner<R> newAnnotationOrParentScanner(Class<? extends Annotation> annotationClass,
            Class<?> parentClass) {
        return new DefaultClassScanner<R>()
                .setClassFilter(ClassFilters.newAnnotationOrParentClassFilter(annotationClass, parentClass));
    }

    public static <R> DefaultClassScanner<R> newAnnotationOrParentScanner(Class<? extends Annotation> annotationClass,
            Class<?> parentClass, String[] packagePrefixes) {
        return new DefaultClassScanner<R>()
                .setClassFilter(ClassFilters.newAnnotationOrParentClassFilter(annotationClass, parentClass))
                .setPackagePrefixes(packagePrefixes);
    }
}
