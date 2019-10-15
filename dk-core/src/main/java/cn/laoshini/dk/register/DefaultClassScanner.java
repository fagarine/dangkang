package cn.laoshini.dk.register;

import java.util.function.Function;
import java.util.jar.JarFile;

import cn.laoshini.dk.common.ResourcesHolder;

/**
 * @author fagarine
 */
public class DefaultClassScanner<R> implements IClassScanner<R> {

    private IClassFilter classFilter = ALWAYS_TRUE;

    private String[] packagePrefixes = ResourcesHolder.getPackagePrefixesAsArray();

    private JarFile jarFile;

    private Function<Class<?>, R> converter;

    @Override
    public IClassFilter classFilter() {
        return classFilter;
    }

    @Override
    public DefaultClassScanner<R> setClassFilter(IClassFilter classFilter) {
        this.classFilter = classFilter;
        return this;
    }

    @Override
    public String[] packagePrefixes() {
        return packagePrefixes;
    }

    @Override
    public DefaultClassScanner<R> setPackagePrefixes(String[] packagePrefixes) {
        this.packagePrefixes = packagePrefixes;
        return this;
    }

    @Override
    public JarFile jarFile() {
        return jarFile;
    }

    @Override
    public DefaultClassScanner<R> setJarFile(JarFile jarFile) {
        this.jarFile = jarFile;
        return this;
    }

    @Override
    public Function<Class<?>, R> converter() {
        return converter;
    }

    @Override
    public DefaultClassScanner<R> setConverter(Function<Class<?>, R> converter) {
        this.converter = converter;
        return this;
    }
}
