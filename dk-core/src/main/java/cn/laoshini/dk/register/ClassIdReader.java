package cn.laoshini.dk.register;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;

import cn.laoshini.dk.util.LogUtil;

/**
 * @author fagarine
 */
public class ClassIdReader {
    private ClassIdReader() {
    }

    /**
     * 创建并返回一个，通过类中的常量或类静态变量，读取类ID的读取器
     *
     * @param idFieldName 类id变量名称
     * @param <I> id类型
     * @return 该方法不会返回null
     */
    public static <I> Function<Class<?>, I> fieldReader(String idFieldName) {
        return clazz -> {
            Field idField;
            try {
                idField = clazz.getDeclaredField(idFieldName);
            } catch (NoSuchFieldException e) {
                LogUtil.debug("类[{}]中没有找到名为[{}]的变量或常量", clazz.getName(), idFieldName);
                return null;
            }

            if (!Modifier.isStatic(idField.getModifiers())) {
                LogUtil.debug("类[{}]中id[{}]必须是静态变量或常量", clazz.getName(), idFieldName);
                return null;
            }

            I id;
            boolean accessible = idField.isAccessible();
            try {
                if (!accessible) {
                    idField.setAccessible(true);
                }
                id = (I) (idField.get(null));
                return id;
            } catch (Exception e) {
                LogUtil.debug("获取[{}]类对应的id出错, field: {}", clazz.getName(), idFieldName);
                return null;
            } finally {
                idField.setAccessible(accessible);
            }
        };
    }

    /**
     * 创建并返回一个，通过类中的静态方法，读取类ID的读取器
     *
     * @param idMethodName 读取类id方法名称
     * @param <I> id类型
     * @return 该方法不会返回null
     */
    public static <I> Function<Class<?>, I> methodReader(String idMethodName) {
        return clazz -> {
            Method idMethod;
            try {
                idMethod = clazz.getDeclaredMethod(idMethodName);
            } catch (NoSuchMethodException e) {
                LogUtil.debug("类[{}]没有提供一个可访问的无参方法获取id, method:{}", clazz.getName(), idMethodName);
                return null;
            }

            if (!Modifier.isStatic(idMethod.getModifiers())) {
                LogUtil.debug("类[{}]中获取id的方法[{}]必须是静态方法", clazz.getName(), idMethodName);
                return null;
            }

            I id;
            boolean accessible = idMethod.isAccessible();
            try {
                if (!accessible) {
                    idMethod.setAccessible(true);
                }
                id = (I) (idMethod.invoke(null));
                return id;
            } catch (Exception e) {
                LogUtil.debug("获取[{}]类对应的id出错, method: {}", clazz.getName(), idMethodName);
                return null;
            } finally {
                idMethod.setAccessible(accessible);
            }
        };
    }

    /**
     * 创建并返回一个，通过类注解中的方法，读取类ID的读取器
     *
     * @param annotationClass 带有读取类id方法的注解类
     * @param idMethodName 读取类id方法名称
     * @param <I> id变量类型
     * @return 该方法不会返回null
     */
    public static <I> Function<Class<?>, I> annotationReader(Class<? extends Annotation> annotationClass,
            String idMethodName) {
        return clazz -> {
            Method idMethod;
            // 在注解中查找获取id的方法
            Annotation annotation = clazz.getAnnotation(annotationClass);
            if (annotation != null) {
                try {
                    idMethod = annotationClass.getMethod(idMethodName);
                } catch (NoSuchMethodException e1) {
                    LogUtil.debug("注解[{}]中没有获取id的方法, method: {}", clazz.getName(), idMethodName);
                    return null;
                }
            } else {
                LogUtil.debug("从注解中获取[{}]类对应的id出错, method: {}", clazz.getName(), idMethodName);
                return null;
            }

            I id;
            try {
                id = (I) (idMethod.invoke(annotation));
                return id;
            } catch (Exception e) {
                LogUtil.debug("从注解中获取[{}]类对应的id出错, method: {}", clazz.getName(), idMethodName);
                return null;
            }
        };
    }

    public static <I> Function<Class<?>, I> methodOrAnnotationReader(String idMethodName,
            Class<? extends Annotation> annotationClass, String annotationIdMethod) {
        return clazz -> {
            Object object = null;
            Method idMethod = null;
            try {
                idMethod = clazz.getDeclaredMethod(idMethodName);
                if (!Modifier.isStatic(idMethod.getModifiers())) {
                    LogUtil.debug("类[{}]中获取id的方法[{}]必须是静态方法", clazz.getName(), idMethodName);
                    return null;
                }
            } catch (NoSuchMethodException e) {
                // 在类中找不到获取id的方法，在注解中查找
                if (annotationClass != null) {
                    Annotation annotation = clazz.getAnnotation(annotationClass);
                    if (annotation != null) {
                        try {
                            idMethod = annotationClass.getMethod(annotationIdMethod);
                            object = annotation;
                        } catch (NoSuchMethodException e1) {
                            // ignore
                        }
                    }
                }

            }

            if (idMethod == null) {
                LogUtil.debug("类[{}]没有提供一个外部可访问的无参方法获取id, method: {}", clazz.getName(), idMethodName);
                return null;
            }

            I id;
            boolean accessible = idMethod.isAccessible();
            try {
                if (!accessible) {
                    idMethod.setAccessible(true);
                }
                id = (I) (idMethod.invoke(object));
                return id;
            } catch (Exception e) {
                LogUtil.debug("获取[{}]类对应的id出错, method: {}", clazz.getName(), idMethodName);
                return null;
            } finally {
                idMethod.setAccessible(accessible);
            }
        };
    }
}
