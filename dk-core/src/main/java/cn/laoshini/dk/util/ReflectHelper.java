package cn.laoshini.dk.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.jar.JarFile;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.constant.GameConstant;
import cn.laoshini.dk.dao.TableKey;
import cn.laoshini.dk.dao.TableMapping;
import cn.laoshini.dk.domain.ExecutorBean;
import cn.laoshini.dk.domain.common.ArrayTuple;
import cn.laoshini.dk.domain.common.MethodDescriptor;
import cn.laoshini.dk.domain.common.Tuple;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.net.handler.IMessageHandler;
import cn.laoshini.dk.net.msg.CustomMsg;
import cn.laoshini.dk.net.msg.ICustomMessage;

/**
 * @author fagarine
 */
public class ReflectHelper {
    private ReflectHelper() {
    }

    /**
     * 记录当前系统支持热加载的Spring注解类型
     */
    @SuppressWarnings("unchecked")
    public static final Class<? extends Annotation>[] SPRING_ANNOTATIONS = new Class[] { Configuration.class,
            Repository.class, Component.class, Service.class, Controller.class };

    /**
     * 类是否已被Spring相关注解标记，具体支持检查的Spring注解类型: {@link #SPRING_ANNOTATIONS}
     *
     * @param clazz 类
     * @return 返回判断结果
     */
    public static boolean isSpringAnnotationPresent(Class<?> clazz) {
        for (Class<? extends Annotation> springAnnotation : SPRING_ANNOTATIONS) {
            if (clazz.isAnnotationPresent(springAnnotation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在jar包中查找并返回所有被Spring注解（具体支持的注解类型详见{@link #SPRING_ANNOTATIONS}）标记了的类
     *
     * @param jarFile jar包文件
     * @param classLoader 类加载器对象
     * @param packageName 包路径
     * @param exclusive 需要排除的类
     * @return 该方法不会返回null
     */
    public static List<Class<?>> getAllSpringAnnotationInJarFile(JarFile jarFile, ClassLoader classLoader,
            String packageName, Collection<Class<?>> exclusive) {
        List<Class<?>> classes = new ArrayList<>();
        for (Class<? extends Annotation> springAnnotation : SPRING_ANNOTATIONS) {
            List<Class<?>> classList = ClassUtil
                    .getAllClassByAnnotationInJarFile(jarFile, classLoader, packageName, springAnnotation);
            if (!classList.isEmpty()) {
                if (exclusive != null && !exclusive.isEmpty()) {
                    classList.removeAll(exclusive);
                }
                classes.addAll(classList);
            }
        }
        return classes;
    }

    public static List<Class<?>> getAllSpringAnnotationInClasspath(ClassLoader classLoader, String[] packageNames,
            Collection<Class<?>> exclusive) {
        Predicate<Class<?>> filter = CollectionUtil.isEmpty(exclusive) ?
                springClassFilter() :
                springClassExclusiveFilter(exclusive);
        List<Class<?>> classes = new ArrayList<>();
        if (packageNames != null && packageNames.length > 0) {
            for (String packageName : packageNames) {
                classes.addAll(ClassUtil.getAllClassInPackage(classLoader, packageName, true, filter));
            }
        }
        return classes;
    }

    private static Predicate<Class<?>> springClassExclusiveFilter(Collection<Class<?>> exclusive) {
        return (clazz) -> (CollectionUtil.isEmpty(exclusive) || !exclusive.contains(clazz))
                && isSpringAnnotationPresent(clazz);
    }

    private static Predicate<Class<?>> springClassFilter() {
        return ReflectHelper::isSpringAnnotationPresent;
    }

    /**
     * 在jar包中查找并返回所有被{@link MessageHandle}注解标记了的类
     *
     * @param jarFile jar包文件
     * @param classLoader 类加载器对象
     * @param packageName 包路径
     * @return 该方法不会返回null
     */
    public static Map<Integer, ExecutorBean<MessageHandle>> getAllMessageHandlerInJarFile(JarFile jarFile,
            ClassLoader classLoader, String packageName) {
        List<Class<?>> classes = ClassUtil
                .getAllClassByAnnotationInJarFile(jarFile, classLoader, packageName, MessageHandle.class);
        return messageHandlerClassProcess(classes);
    }

    /**
     * 在指定classLoader中查找并返回所有被{@link MessageHandle}注解标记了的类
     *
     * @param classLoader 类加载器对象
     * @param packageNames 包路径
     * @return 该方法不会返回null
     */
    public static Map<Integer, ExecutorBean<MessageHandle>> getAllMessageHandler(ClassLoader classLoader,
            String[] packageNames) {
        List<Class<?>> classes = ClassUtil
                .getClassByAnnotationInPackages(classLoader, packageNames, MessageHandle.class);
        return messageHandlerClassProcess(classes);
    }

    private static Map<Integer, ExecutorBean<MessageHandle>> messageHandlerClassProcess(List<Class<?>> classes) {
        Map<Integer, ExecutorBean<MessageHandle>> map = new HashMap<>(classes.size());
        for (Class<?> clazz : classes) {
            if (clazz.isAssignableFrom(IMessageHandler.class)) {
                throw new BusinessException("class.not.handler",
                        String.format("消息处理handler[%s]没有实现[%s]接口，该接口必须实现", clazz.getName(),
                                IMessageHandler.class.getName()));
            }

            MessageHandle annotation = clazz.getAnnotation(MessageHandle.class);
            if (map.containsKey(annotation.id())) {
                ExecutorBean bean = map.get(annotation.id());
                throw new BusinessException("handler.id.duplicate",
                        String.format("多个handler注册了同一协议号[%d], handler1: %s, handler2: %s", annotation.id(),
                                bean.getExecutorClassName(), clazz.getName()));
            }

            map.put(annotation.id(), new ExecutorBean<>(annotation, GameConstant.HANDLER_ACTION_METHOD, clazz));

        }
        return map;
    }

    /**
     * 获取jar包中所有的自定义消息类
     *
     * @param jarFile jar包文件
     * @param classLoader 类加载器对象
     * @param packageName 包路径
     * @return 该方法不会返回null
     */
    public static Map<Integer, Class<? extends ICustomMessage>> getAllCustomMessageInJarFile(JarFile jarFile,
            ClassLoader classLoader, String packageName) {
        List<Class<?>> classes = ClassUtil
                .getAllClassByAnnotationInJarFile(jarFile, classLoader, packageName, CustomMsg.class);
        return customMsgClassProcess(classes);
    }

    /**
     * 获取指定classLoader中所有的自定义消息类
     *
     * @param classLoader 类加载器对象
     * @param packageNames 包路径
     * @return 该方法不会返回null
     */
    public static Map<Integer, Class<? extends ICustomMessage>> getAllCustomMessage(ClassLoader classLoader,
            String[] packageNames) {
        List<Class<?>> classes = ClassUtil.getClassByAnnotationInPackages(classLoader, packageNames, CustomMsg.class);
        return customMsgClassProcess(classes);
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, Class<? extends ICustomMessage>> customMsgClassProcess(List<Class<?>> classes) {
        Map<Integer, Class<? extends ICustomMessage>> map = new HashMap<>(classes.size());
        for (Class<?> clazz : classes) {
            if (clazz.isAssignableFrom(ICustomMessage.class)) {
                throw new BusinessException("class.not.message",
                        String.format("自定义消息类[%s]没有实现[%s]接口，该接口必须实现", clazz.getName(), ICustomMessage.class.getName()));
            }

            CustomMsg annotation = clazz.getAnnotation(CustomMsg.class);
            if (map.containsKey(annotation.id())) {
                Class cls = map.get(annotation.id());
                throw new BusinessException("custom.message.duplicate",
                        String.format("多个自定义消息类注册了同一消息id[%d], class1: %s, class2: %s", annotation.id(), cls.getName(),
                                clazz.getName()));
            }
            map.put(annotation.id(), (Class<? extends ICustomMessage>) clazz);
        }
        return map;
    }

    /**
     * 在jar包中查找并返回所有被{@link TableMapping}注解标记了的类
     *
     * @param jarFile jar包文件
     * @param classLoader 类加载器对象
     * @param packageName 包路径
     * @return 该方法不会返回null
     */
    public static Map<String, Class<?>> getAllTableClassInJarFile(JarFile jarFile, ClassLoader classLoader,
            String packageName) {
        List<Class<?>> classes = ClassUtil
                .getAllClassByAnnotationInJarFile(jarFile, classLoader, packageName, TableMapping.class);
        return constTableClassProcess(classes);
    }

    /**
     * 在指定classLoader中查找并返回所有被{@link TableMapping}注解标记了的类
     *
     * @param classLoader 类加载器对象
     * @param packageNames 包路径
     * @return 该方法不会返回null
     */
    public static Map<String, Class<?>> getAllTableClass(ClassLoader classLoader, String[] packageNames) {
        List<Class<?>> classes = ClassUtil
                .getClassByAnnotationInPackages(classLoader, packageNames, TableMapping.class);
        return constTableClassProcess(classes);
    }

    private static Map<String, Class<?>> constTableClassProcess(List<Class<?>> classes) {
        Map<String, Class<?>> map = new HashMap<>(classes.size());
        for (Class<?> clazz : classes) {
            TableMapping annotation = clazz.getAnnotation(TableMapping.class);
            String tableName = getTableMappingName(annotation, clazz.getSimpleName());
            if (map.containsKey(tableName)) {
                Class cls = map.get(tableName);
                throw new BusinessException("table.mapping.duplicate",
                        String.format("多个常量表单类注册了同一名称[%s], class1: %s, class2: %s", tableName, cls.getName(),
                                clazz.getName()));
            }
            map.put(tableName, clazz);
        }
        return map;
    }

    public static String getTableMappingName(TableMapping tableMapping, String className) {
        if (StringUtil.isNotEmptyString(tableMapping.value())) {
            return tableMapping.value();
        }
        return className;
    }

    /**
     * 获取消息处理handler的泛型类型
     *
     * @param handlerClass handler类
     * @return 该方法可能返回null
     */
    public static Class<?> getMessageHandlerGenericType(Class<? extends IMessageHandler> handlerClass) {
        Type type = ReflectUtil.getAssignedInterfaceGenericType(handlerClass, IMessageHandler.class);
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return null;
    }

    public static List<MethodDescriptor> getMethods(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }

        Method[] methods = clazz.getMethods();
        List<MethodDescriptor> list = new ArrayList<>(methods.length);
        for (Method method : methods) {
            MethodDescriptor descriptor = new MethodDescriptor();
            descriptor.setName(method.toGenericString());
            if (method.getParameterCount() > 0) {
                descriptor.setParams(new ArrayList<>(method.getParameterCount()));
                for (Parameter parameter : method.getParameters()) {
                    descriptor.getParams().add(new Tuple<>(parameter.getType().getName(), parameter.getName()));
                }
            }
            list.add(descriptor);
        }
        return list;
    }

    /**
     * 获取方法的参数信息
     *
     * @param method 方法对象
     * @return 该方法不会返回null
     */
    public static ArrayTuple<String, String> getMethodParams(Method method) {
        int count = method.getParameterCount();
        if (count == 0) {
            return new ArrayTuple<>();
        }

        String[] types = new String[count];
        String[] names = new String[count];
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < names.length; i++) {
            Parameter parameter = parameters[i];
            types[i] = parameter.getType().getName();
            names[i] = parameter.getName();
        }
        return new ArrayTuple<>(types, names);
    }

    /**
     * 从表对应的实体类对象中，拼接键值返回
     *
     * @param tableName 表名称
     * @param obj 实体类对象，调用时一定要保证传入对象的类已被@{@link TableMapping}标记
     * @return 该方法不会返回null
     */
    public static String getTableKey(String tableName, Object obj) {
        StringBuilder sb = new StringBuilder(tableName);
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(TableKey.class)) {
                boolean access = field.isAccessible();
                field.setAccessible(true);
                try {
                    sb.append(Constants.UNDERLINE);
                    sb.append(field.get(obj));
                } catch (IllegalAccessException e) {
                    throw new BusinessException("append.key.error",
                            String.format("拼接对象key出错, bean:%s, field:%s", obj, field.getName()), e);
                } finally {
                    field.setAccessible(access);
                }
            }
        }
        return sb.toString();
    }

}
