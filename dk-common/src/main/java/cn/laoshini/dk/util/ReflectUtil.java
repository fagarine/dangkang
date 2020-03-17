package cn.laoshini.dk.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import cn.laoshini.dk.exception.BusinessException;

/**
 * @author fagarine
 */
public class ReflectUtil {

    /**
     * 获取类来自于指定父类或接口的泛型类型（Type[]形式返回，带有泛型数据详细信息，需要自己根据此做进一步分析）
     *
     * @param clazz 子类
     * @param superClass 父类或接口类
     * @param <SuperType> 父类或接口的类型
     * @return 该方法可能返回null
     */
    public static <SuperType> Type[] getSuperClassGenericTypes(Class<? extends SuperType> clazz,
            Class<SuperType> superClass) {
        ParameterizedType parameterizedType = null;
        for (Class<?> classInterface : clazz.getInterfaces()) {
            if (superClass.isAssignableFrom(classInterface)) {
                for (Type type : clazz.getGenericInterfaces()) {
                    if (type instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) type;
                        if (classInterface.equals(pt.getRawType())) {
                            parameterizedType = pt;
                        }
                    }
                }
            }
        }

        if (parameterizedType == null) {
            if (superClass.isAssignableFrom(clazz.getSuperclass())) {
                Type superType = clazz.getGenericSuperclass();
                if (superType instanceof ParameterizedType) {
                    parameterizedType = (ParameterizedType) superType;
                } else {
                    return getSuperClassGenericTypes((Class<? extends SuperType>) clazz.getSuperclass(), superClass);
                }
            }
        }

        if (parameterizedType != null) {
            return parameterizedType.getActualTypeArguments();
        }
        return null;
    }

    /**
     * 获取类来自于指定父类或接口的泛型类型（Class[]形式返回，适用于在父类或接口的定义中，包含多个泛型类型的类，比如Map）
     *
     * @param clazz 子类
     * @param superClass 父类或接口类
     * @param <SuperType> 父类或接口的类型
     * @return 该方法可能返回null
     */
    public static <SuperType> Class[] getSuperClassGenericClasses(Class<? extends SuperType> clazz,
            Class<SuperType> superClass) {
        Type[] genericTypes = getSuperClassGenericTypes(clazz, superClass);
        if (CollectionUtil.isEmpty(genericTypes)) {
            return null;
        }

        Class[] classes = new Class[genericTypes.length];
        for (int i = 0; i < genericTypes.length; i++) {
            classes[i] = genericTypeToClass(genericTypes[i]);
        }
        return classes;
    }

    /**
     * 获取类来自于指定父类或接口的泛型类型（Class形式返回，适用于只包含父类或接口类只定义了单个泛型的类，比如List）
     *
     * @param clazz 子类
     * @param superClass 父类或接口类
     * @param <SuperType> 父类或接口的类型
     * @return 该方法可能返回null
     */
    public static <SuperType> Class<?> getSuperClassGenericClass(Class<? extends SuperType> clazz,
            Class<SuperType> superClass) {
        Type[] genericTypes = getSuperClassGenericTypes(clazz, superClass);
        if (CollectionUtil.isEmpty(genericTypes)) {
            return null;
        }

        return genericTypeToClass(genericTypes[0]);
    }

    /**
     * 根据传入的泛型类描述信息，获取其直接泛型类型
     *
     * @param genericType 描述泛型信息的类型对象
     * @return 返回获取到的直接泛型类型，该方法可能返回null
     */
    public static Class<?> genericTypeToClass(Type genericType) {
        Class<?> clazz = null;
        if (genericType instanceof Class) {
            clazz = (Class) genericType;
        } else if (genericType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) genericType;
            if (CollectionUtil.isNotEmpty(wildcardType.getLowerBounds())) {
                Type type = wildcardType.getLowerBounds()[0];
                clazz = type instanceof Class ? (Class) type : null;
            } else if (CollectionUtil.isNotEmpty(wildcardType.getUpperBounds())) {
                Type type = wildcardType.getUpperBounds()[0];
                clazz = type instanceof Class ? (Class) type : null;
            }
        } else if (genericType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) genericType).getRawType();
            clazz = rawType instanceof Class ? (Class) rawType : null;
        }
        return clazz;
    }

    /**
     * 获取Field的泛型类型（适用于只有一个泛型类型的Field）
     *
     * @param field field
     * @return 如果获取不到，将会返回null
     */
    public static Class getFieldGenericType(Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type genericType = parameterizedType.getActualTypeArguments()[0];
            return genericTypeToClass(genericType);
        }
        return null;
    }

    public static Class getCollectionFieldGenericType(Field field) {
        if (field == null || !Collection.class.isAssignableFrom(field.getType())) {
            return null;
        }

        return getFieldGenericType(field);
    }

    /**
     * 获取Field的泛型类型（适用于包含多个泛型类型的Field）
     *
     * @param field field
     * @return 如果获取不到，将会返回null
     */
    public static Class[] getFieldGenericTypes(Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type[] genericTypes = parameterizedType.getActualTypeArguments();
            Class[] classes = new Class[genericTypes.length];
            for (int i = 0; i < genericTypes.length; i++) {
                classes[i] = genericTypeToClass(genericTypes[i]);
            }
            return classes;
        }
        return null;
    }

    public static Class[] getMapFieldGenericType(Field field) {
        if (field == null || !Map.class.isAssignableFrom(field.getType())) {
            return null;
        }

        return getFieldGenericTypes(field);
    }

    /**
     * 创建对象并返回，根据传入的参数类型调用对应的构造器创建对象，适用于无参构造和无基本类型做为参数的构造函数。
     * <p>
     * 注意：如果构造器中包含基本类型，如int，进入方法时，将会被转为Integer，方法自动识别出的也会是Integer类型，将导致无法找到对应的构造器，
     * 如果要使用带有基本类型的构造器，请使用{@link #newInstanceByType(Class, Object[], Class[])}方法，手动传入参数类型
     * </p>
     *
     * @param clazz 类
     * @param initArgs 构造函数需要的参数
     * @param <T> 类的类型
     * @return 返回创建后的对象，如果类为空或类中没有对应的构造方法，返回null
     */
    public static <T> T newInstance(Class<T> clazz, Object... initArgs) {
        return newInstanceByType(clazz, initArgs, getParamClasses(initArgs));
    }

    /**
     * 创建对象并返回，根据传入的参数类型调用对应的构造器创建对象
     *
     * @param clazz 类
     * @param initArgs 构造函数需要的参数，如果调用无参构造，可以传入null
     * @param argTypes 构造函数需要的参数类型，将根据该值中的类型查找对应的构造器，如果调用无参构造，可以传入null
     * @param <T> 类的类型
     * @return 返回创建后的对象，如果类为空或类中没有对应的构造方法，返回null
     */
    public static <T> T newInstanceByType(Class<T> clazz, Object[] initArgs, Class<?>[] argTypes) {
        if (clazz != null) {
            try {
                initArgs = initArgs == null ? new Object[0] : initArgs;
                argTypes = argTypes == null ? new Class[0] : argTypes;
                Constructor<T> constructor = clazz.getDeclaredConstructor(argTypes);
                boolean accessible = constructor.isAccessible();
                constructor.setAccessible(true);

                try {
                    return constructor.newInstance(initArgs);
                } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    e.printStackTrace();
                } finally {
                    constructor.setAccessible(accessible);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Class<?>[] getParamClasses(Object[] params) {
        Class<?>[] paramClasses;
        if (CollectionUtil.isNotEmpty(params)) {
            paramClasses = new Class<?>[params.length];
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                paramClasses[i] = param.getClass();
            }
        } else {
            paramClasses = new Class<?>[0];
        }
        return paramClasses;
    }

    public static List<String> getClassAllMethod(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }

        Method[] methods = clazz.getMethods();
        List<String> methodNames = new ArrayList<>(methods.length);
        for (Method method : methods) {
            String detail = method.toString();
            methodNames.add(method.toString());
        }
        return methodNames;
    }

    /**
     * 类中是否有指定方法（不查找父类）
     *
     * @param clazz 类
     * @param methodName 方法名
     * @param parameterTypes 参数表
     * @return 返回判断结果
     */
    public static boolean containsMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        if (null == clazz || StringUtil.isEmptyString(methodName)) {
            return false;
        }

        try {
            return clazz.getMethod(methodName, parameterTypes) != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        if (null == clazz || StringUtil.isEmptyString(methodName)) {
            throw new IllegalArgumentException("class or method name must not be null");
        }

        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (Exception e) {
            throw new BusinessException("method.not.find",
                    String.format("找不到[%s]类的[%s]方法，参数信息:%s", clazz.getName(), methodName,
                            parameterTypes == null ? "" : Arrays.toString(parameterTypes)));
        }
    }

    /**
     * 返回枚举类的所有枚举值
     *
     * @param enumType 枚举类
     * @param <E> 枚举类型
     * @return 返回所有枚举值
     */
    public static <E extends Enum> E[] getEnumValues(Class<E> enumType) {
        try {
            Method method = enumType.getDeclaredMethod("values");
            boolean access = method.isAccessible();
            method.setAccessible(true);
            try {
                return (E[]) method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                method.setAccessible(access);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取单例类的实例对象
     *
     * @param clazz 单例类
     * @param <T> 单例类型
     * @return 返回实例对象，如果找不到实例对象，将会返回null
     */
    public static <T> T getSingletonInstance(Class<T> clazz) {
        if (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(clazz)) {
                    return (T) getFieldValue(null, field);
                }
            }
        }
        return null;
    }

    /**
     * 反射执行静态方法
     *
     * @param clazz 类
     * @param methodName 静态方法名
     * @param params 传入参数
     * @return 返回执行结果
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Object... params)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method m = clazz.getMethod(methodName, getParamClasses(params));
        return m.invoke(null, params);
    }

    /**
     * 反射执行方法
     *
     * @param obj 对象
     * @param methodName 方法名
     * @param params 传入参数
     * @return 返回执行结果
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static Object invokeMethod(Object obj, String methodName, Object... params)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Method m = obj.getClass().getDeclaredMethod(methodName, getParamClasses(params));
        return m.invoke(obj, params);
    }

    /**
     * 反射执行方法（忽略方法的访问限制）
     *
     * @param obj 对象或类
     * @param methodName 方法名
     * @param params 传入参数
     * @return 返回执行结果
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static Object invokeMethodAnyway(Object obj, String methodName, Object... params)
            throws NoSuchMethodException, InvocationTargetException {
        Class<?> clazz = obj.getClass();
        if (obj instanceof Class) {
            clazz = (Class<?>) obj;
        }
        Method m = clazz.getDeclaredMethod(methodName, getParamClasses(params));
        boolean access = m.isAccessible();
        m.setAccessible(true);
        try {
            return m.invoke(obj, params);
        } catch (IllegalAccessException e) {
            // ignore
            return null;
        } finally {
            m.setAccessible(access);
        }
    }

    /**
     * 指定对象来自于Object类的{@link Object#clone() clone()}方法，注意：使用该方法，必须保证类实现了{@link Cloneable}接口
     *
     * @param obj 对象
     * @param <T> 对象类型
     * @return 如果拷贝成功，返回对象的浅拷贝
     * @throws InvocationTargetException 如果对象的类没有实现Cloneable接口，将会抛出该异常
     */
    public static <T> T invokeClone(T obj) throws InvocationTargetException {
        Class clazz = obj.getClass();
        while (!Object.class.equals(clazz)) {
            clazz = clazz.getSuperclass();
        }

        try {
            Method method = clazz.getDeclaredMethod("clone");
            boolean access = method.isAccessible();
            method.setAccessible(true);
            try {
                return (T) method.invoke(obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                method.setAccessible(access);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取类中的静态参数的值，包括常量
     *
     * @param className 类全名（包含包路径的全名）
     * @param paramName 参数名
     * @return 返回参数值
     * @throws ClassNotFoundException
     */
    public static Object getStaticFieldValue(String className, String paramName) throws ClassNotFoundException {
        if (StringUtil.isEmptyString(className)) {
            throw new IllegalArgumentException("传入的类名为空");
        }

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class<?> clazz = loader.loadClass(className);
        return getStaticFieldValue(clazz, paramName);
    }

    /**
     * 获取类中的静态变量的值，包括常量
     *
     * @param clazz 类
     * @param paramName 参数名
     * @return
     */
    public static Object getStaticFieldValue(Class<?> clazz, String paramName) {
        // 获取静态变量或常量的值，不需要传入对象
        return getFieldValue(clazz, paramName, null);
    }

    /**
     * 获取类或对象中的某个参数的值
     *
     * @param clazz 类
     * @param paramName 参数
     * @param obj 类的实例对象
     * @return
     */
    public static Object getFieldValue(Class<?> clazz, String paramName, Object obj) {
        if (null == clazz) {
            if (null == obj) {
                throw new IllegalArgumentException("类不能为空");
            }

            clazz = obj.getClass();
        } else if (null != obj) {
            if (!clazz.getName().equals(obj.getClass().getName())) {
                throw new IllegalArgumentException("传入的类和实例对象不匹配");
            }
        }

        if (StringUtil.isEmptyString(paramName)) {
            throw new IllegalArgumentException("参数名不能为空, class:" + clazz.getName() + ", paramName:" + paramName);
        }

        Field field;
        try {
            field = clazz.getDeclaredField(paramName);
        } catch (NoSuchFieldException e) {
            return null;
        }

        return getFieldValue(obj, field);
    }

    public static Object getFieldValue(Object obj, Field field) {
        boolean access = field.isAccessible();
        field.setAccessible(true);
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            field.setAccessible(access);
        }
        return null;
    }

    public static <T> T[] getArrayFieldValue(Object object, Field filed) {
        if (object == null || filed == null || !filed.getType().isArray()) {
            return null;
        }

        return (T[]) getFieldValue(object, filed);
    }

    /**
     * 获取对象中所有变量的值
     *
     * @param obj 实例对象
     * @return 该方法可能返回null
     * @throws IllegalAccessException
     */
    public static List<Object> getAllFieldValue(Object obj) throws IllegalAccessException {
        return getAllFieldValue(obj, false);
    }

    public static List<Object> getAllFieldValue(Object obj, boolean nullable) throws IllegalAccessException {
        if (obj == null) {
            return null;
        }

        Field[] fields = obj.getClass().getDeclaredFields();
        List<Object> fieldValues = new ArrayList<>(fields.length);
        for (Field field : fields) {
            boolean access = field.isAccessible();
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(obj);
                if (value == null && !nullable) {
                    fieldValues.add(defaultValue(field.getType()));
                } else {
                    fieldValues.add(value);
                }
            } catch (IllegalAccessException e) {
                throw e;
            } finally {
                field.setAccessible(access);
            }
        }
        return fieldValues;
    }

    private static Object defaultValue(Class<?> clazz) {
        if (Byte.class.equals(clazz) || Short.class.equals(clazz) || Float.class.equals(clazz) || Double.class
                .equals(clazz) || Integer.class.equals(clazz) || Long.class.equals(clazz)) {
            return 0;
        } else if (Boolean.class.equals(clazz)) {
            return false;
        }
        return null;
    }

    /**
     * 对象是否包含所有指定变量，且变量的值相等，用于对象过滤
     *
     * @param obj 对象实例
     * @param filter 过滤条件
     * @return 返回是否完全符合条件
     */
    public static boolean containsAssignedValueFields(Object obj, Map<String, Object> filter) {
        if (obj == null) {
            return false;
        }

        if (filter == null || filter.isEmpty()) {
            return true;
        }

        Class<?> clazz = obj.getClass();
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            try {
                Field field = clazz.getDeclaredField(entry.getKey());
                boolean access = field.isAccessible();
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if (!entry.getValue().equals(value)) {
                        return false;
                    }
                } catch (IllegalAccessException e) {
                    // 找到变量，但是不可访问，视为不符合条件
                    return false;
                } finally {
                    field.setAccessible(access);
                }
            } catch (NoSuchFieldException e) {
                // 找不到的变量名，忽略
            }
        }
        return true;
    }

    public static void setFieldValue(Object object, String fieldName, Object value) {
        if (object == null || fieldName == null) {
            return;
        }

        Field field;
        try {
            field = object.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return;
        }

        setFieldValue(object, field, value);
    }

    public static void setFieldValue(Object object, Field field, Object value) {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            field.setAccessible(accessible);
        }
    }

    public static void setStaticFiledValue(Class<?> clazz, String fieldName, Object value) {
        try {
            setStaticFiledValue(clazz.getDeclaredField(fieldName), value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void setStaticFiledValue(Field field, Object value) {
        setFieldValue(null, field, value);
    }

    /**
     * 拷贝源对象中指定变量的值，到目标对象中的同名变量
     *
     * @param source 源对象
     * @param target 目标对象
     * @param sourceField 指定变量
     */
    public static void copyField(Object source, Object target, Field sourceField) {
        if (source == null || target == null || sourceField == null) {
            return;
        }

        String fieldName = sourceField.getName();
        Field targetField;
        try {
            targetField = target.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return;
        }

        boolean sourceAccessible = sourceField.isAccessible();
        boolean targetAccessible = targetField.isAccessible();
        sourceField.setAccessible(true);
        targetField.setAccessible(true);

        try {
            targetField.set(target, sourceField.get(source));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            sourceField.setAccessible(sourceAccessible);
            targetField.setAccessible(targetAccessible);
        }
    }

    public static List<Field> getFields(Class<?> clazz, Predicate<Field> fieldFilter) {
        if (clazz == null || fieldFilter == null) {
            return Collections.emptyList();
        }

        List<Field> fields = new LinkedList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (fieldFilter.test(field)) {
                fields.add(field);
            }
        }
        return fields;
    }

    public static List<Field> getAssignedAnnotationFields(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        if (clazz == null || annotationClass == null) {
            return Collections.emptyList();
        }

        return getFields(clazz, f -> f.isAnnotationPresent(annotationClass));
    }

    /**
     * 获取类的无参构造方法
     *
     * @param clazz 类
     * @return 返回类的无参构造器
     */
    protected Constructor<?> getNoArgsConstructor(Class<?> clazz) {
        Constructor<?> constructor;
        try {
            constructor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(clazz.getName() + "类没有提供一个无参的构造方法", e);
        }
        return constructor;
    }

    protected Object newInstance(Constructor<?> constructor) {
        if (constructor == null) {
            throw new IllegalArgumentException("构造器不能为空");
        }

        try {
            return constructor.newInstance();
        } catch (Exception e) {
            String className = constructor.getDeclaringClass().getName();
            throw new IllegalArgumentException(className + "类没有提供一个公共的无参构造方法", e);
        }
    }
}
