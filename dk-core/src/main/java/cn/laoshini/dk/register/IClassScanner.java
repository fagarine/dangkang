package cn.laoshini.dk.register;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.jar.JarFile;

import cn.laoshini.dk.common.ResourcesHolder;
import cn.laoshini.dk.util.ClassUtil;

/**
 * @param <R> 类被扫描到后的返回结果类型（如果不使用{@link #converter() 转换器}，则该类型就是Class）
 * @author fagarine
 */
public interface IClassScanner<R> {

    IClassFilter ALWAYS_TRUE = clazz -> true;

    /**
     * 类过滤器，满足条件的类返回true
     *
     * @return 返回类过滤器
     */
    default IClassFilter classFilter() {
        return ALWAYS_TRUE;
    }

    IClassScanner<R> setClassFilter(IClassFilter classFilter);

    /**
     * 获取类扫描路径（包路径前缀）
     *
     * @return 该方法不应返回null
     */
    default String[] packagePrefixes() {
        return ResourcesHolder.getPackagePrefixesAsArray();
    }

    default IClassScanner<R> setPackagePrefixes(String[] packagePrefixes) {
        return this;
    }

    default JarFile jarFile() {
        return null;
    }

    default IClassScanner<R> setJarFile(JarFile jarFile) {
        return this;
    }

    default Function<Class<?>, R> converter() {
        return null;
    }

    default IClassScanner<R> setConverter(Function<Class<?>, R> converter) {
        return this;
    }

    /**
     * 根据扫描设置，查找所有符合条件的类
     *
     * @param classLoader 类加载器，只在该类加载器中查找类
     * @return 本方法不会返回null
     */
    default List<R> findClasses(ClassLoader classLoader) {
        List<Class<?>> classes;
        if (jarFile() != null) {
            classes = ClassUtil.getAllClassInJarFile(jarFile(), classLoader, packagePrefixes(), classFilter());
        } else {
            classes = ClassUtil.getAllClassInPackages(classLoader, packagePrefixes(), classFilter());
        }

        List<R> result;
        if (converter() != null) {
            R r;
            result = new ArrayList<>(classes.size());
            for (Class<?> clazz : classes) {
                r = converter().apply(clazz);
                if (r != null) {
                    result.add(r);
                }
            }
        } else {
            result = (List<R>) classes;
        }
        return result;
    }

    static <R> DefaultClassScanner<R> defaultScanner() {
        return new DefaultClassScanner<>();
    }

}
