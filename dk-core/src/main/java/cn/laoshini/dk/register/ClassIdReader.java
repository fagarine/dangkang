package cn.laoshini.dk.register;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Function;

import cn.laoshini.dk.exception.BusinessException;

/**
 * @author fagarine
 */
public class ClassIdReader {
    private ClassIdReader() {
    }

    public static <I> Function<Class<?>, I> methodReader(String idMethodName) {
        return clazz -> {
            Constructor constructor;
            try {
                constructor = clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new BusinessException("no.valid.constructor",
                        String.format("类[%s]没有提供一个外部可访问的无参构造方法", clazz.getName()));
            }

            Object object;
            try {
                object = constructor.newInstance();
            } catch (Exception e) {
                throw new BusinessException("execute.constructor.fail",
                        String.format("调用类[%s]的无参构造方法出错", clazz.getName()), e);
            }

            Method idMethod;
            try {
                idMethod = clazz.getMethod(idMethodName);
            } catch (NoSuchMethodException e) {
                String msg = String.format("类[%s]没有提供一个外部可访问的无参方法获取id, method:%s", clazz.getName(), idMethodName);
                throw new BusinessException("id.method.missing", msg);
            }

            I id;
            try {
                id = (I) (idMethod.invoke(object));
            } catch (Exception e) {
                String msg = String.format("获取[%s]类对应的id出错,  method:%s", clazz.getName(), idMethodName);
                throw new BusinessException("id.method.error", msg, e);
            }

            if (id == null) {
                throw new BusinessException("id.is.null", "id不能为空, class:" + clazz.getName());
            }
            return id;
        };
    }

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
                    String msg = String.format("注解[%s]中没有获取id的方法, method:%s", annotationClass.getName(), idMethodName);
                    throw new BusinessException("id.method.missing", msg);
                }
            } else {
                throw new BusinessException("id.annotation.missing",
                        String.format("类[%s]未被注解[%s]标记，无法获取其对应的id", clazz.getName(), annotationClass.getName()));
            }

            I id;
            try {
                id = (I) (idMethod.invoke(annotation));
            } catch (Exception e) {
                String msg = String.format("从注解中获取[%s]类对应的id出错,  method:%s", clazz.getName(), idMethodName);
                throw new BusinessException("id.method.error", msg, e);
            }

            if (id == null) {
                throw new BusinessException("id.is.null", "id不能为空, class:" + clazz.getName());
            }
            return id;
        };
    }

    public static <I> Function<Class<?>, I> methodOrAnnotationReader(String idMethodName,
            Class<? extends Annotation> annotationClass, String annotationIdMethod) {
        return clazz -> {
            Constructor constructor;
            try {
                constructor = clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new BusinessException("no.valid.constructor",
                        String.format("类[%s]没有提供一个外部可访问的无参构造方法", clazz.getName()));
            }

            Object object;
            try {
                object = constructor.newInstance();
            } catch (Exception e) {
                throw new BusinessException("execute.constructor.fail",
                        String.format("调用类[%s]的无参构造方法出错", clazz.getName()), e);
            }

            Method idMethod = null;
            try {
                idMethod = clazz.getMethod(idMethodName);
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
                String msg = String.format("类[%s]没有提供一个外部可访问的无参方法获取id, method:%s", clazz.getName(), idMethodName);
                throw new BusinessException("id.method.missing", msg);
            }

            I id;
            try {
                id = (I) (idMethod.invoke(object));
            } catch (Exception e) {
                String msg = String.format("获取[%s]类对应的id出错,  method:%s", clazz.getName(), idMethod.getName());
                throw new BusinessException("id.method.error", msg, e);
            }

            if (id == null) {
                throw new BusinessException("id.is.null", "id不能为空, class:" + clazz.getName());
            }
            return id;
        };
    }
}
