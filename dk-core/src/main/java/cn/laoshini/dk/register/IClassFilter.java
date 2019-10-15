package cn.laoshini.dk.register;

import java.util.function.Predicate;

/**
 * 类扫描过滤器
 *
 * @author fagarine
 */
@FunctionalInterface
public interface IClassFilter extends Predicate<Class<?>> {

}
