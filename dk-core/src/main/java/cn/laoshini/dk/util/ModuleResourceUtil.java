package cn.laoshini.dk.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.BeansException;

import cn.laoshini.dk.common.SpringContextHolder;

/**
 * 模块数据资源操作相关工具类，适用于模块热更新时的数据转移
 *
 * @author fagarine
 */
public class ModuleResourceUtil {
    private ModuleResourceUtil() {
    }

    public static Object[] getResourceHolderByType(Class<?> clazz) {
        if (clazz.isEnum()) {
            // 枚举类，返回所有枚举值
            return ReflectUtil.getEnumValues((Class<Enum>) clazz);
        } else {
            // 单例类，返回单例对象
            Object instance = ReflectUtil.getSingletonInstance(clazz);
            if (instance != null) {
                return new Object[] { instance };
            }

            // 静态数据类，直接返回类
            return new Object[] { clazz };
        }
    }

    public static String toHolderKey(Object holder, String name) {
        if (holder instanceof Class) {
            return ((Class) holder).getName();
        }
        return holder.getClass().getName() + "." + name;
    }

    /**
     * 深度拷贝，只执行纯数据拷贝，涉及到得依赖类使用目标对象的类加载器加载
     * <p>
     * 该方法用于两个具有相同或相似变量名的类拷贝，主要用于模块热更新时的数据保留<br>
     * 该方法使用反射拷贝对象中的全局变量，不涉及方法，相同变量名的变量类型必须一直（基本类型可以兼容装箱拆箱）
     * </p>
     *
     * @param source 被拷贝对象
     * @param target 拷贝到的目标对象
     * @param <T> 目标对象的类型
     * @return 返回拷贝后的目标对象
     */
    public static <T> T deepCopy(Object source, T target) {
        if (source == null || target == null) {
            return null;
        }

        Class<?> targetType = target.getClass();
        Set<String> exclusive = SpringUtils.isSpringBeanClass(targetType) ?
                SpringUtils.parseClassSpringDepends(targetType) :
                null;
        return deepCopyByExclusive(source, target, exclusive);
    }

    public static <T> T deepCopyByExclusive(Object source, T target, Set<String> exclusiveFields) {
        if (source == null || target == null) {
            return null;
        }

        Class<?> sourceType = source.getClass();
        Class<?> targetType = target.getClass();
        // 如果目标对象与拷贝对象的类加载器不一样，目标对象的依赖类型使用目标对象自己的加载器加载类
        ClassLoader cl = targetType.getClassLoader();
        for (Field field : sourceType.getDeclaredFields()) {
            String fieldName = field.getName();
            if (Modifier.isFinal(field.getModifiers()) || (exclusiveFields != null && exclusiveFields
                    .contains(fieldName))) {
                continue;
            }

            try {
                SpringContextHolder.getBean(fieldName);
                // 变量为Spring托管对象，跳过拷贝
                continue;
            } catch (BeansException e) {
                // ignore
            }

            Field targetField;
            Class<?> fieldType = field.getType();
            try {
                targetField = targetType.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                LogUtil.debug("目标类[{}]中找不到变量名[{}]，跳过拷贝", targetType.getName(), fieldName);
                continue;
            }

            Object value = ReflectUtil.getFieldValue(source, field);
            if (!fieldType.equals(targetField.getType())) {
                // 相同名称的变量名，如果变量的类型不同，检查是否是基本类型，如果是基本类型，兼容转换
                if (TypeUtil.canTransfer(fieldType, targetField.getType())) {
                    ReflectUtil.setFieldValue(target, fieldName, TypeUtil.basicTransfer(value, targetField.getType()));
                } else {
                    LogUtil.debug("变量[{}]的类型[{}]与目标变量的类型[{}]不一致，跳过拷贝", fieldName, fieldType, targetField.getType());
                }
                continue;
            }

            Class[] genericTypes = getFieldGenericTypes(field, fieldType);
            Object copiedValue = deepCopyValue(value, cl, genericTypes);
            ReflectUtil.setFieldValue(target, fieldName, copiedValue);
        }

        return target;
    }

    public static void deepCopyByClass(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == null || targetType == null) {
            return;
        }

        // 如果目标对象与拷贝对象的类加载器不一样，目标对象的依赖类型使用目标对象自己的加载器加载类
        ClassLoader cl = targetType.getClassLoader();
        for (Field field : sourceType.getDeclaredFields()) {
            if (Modifier.isFinal(field.getModifiers()) || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            String fieldName = field.getName();
            try {
                SpringContextHolder.getBean(fieldName);
                // 变量为Spring托管对象，跳过拷贝
                continue;
            } catch (BeansException e) {
                // ignore
            }

            Field targetField;
            Class<?> fieldType = field.getType();
            try {
                targetField = targetType.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                LogUtil.debug("目标类[{}]中找不到变量名[{}]，跳过拷贝", targetType.getName(), fieldName);
                continue;
            }

            Object value = ReflectUtil.getStaticFieldValue(sourceType, fieldName);
            if (!fieldType.equals(targetField.getType())) {
                // 相同名称的变量名，如果变量的类型不同，检查是否是基本类型，如果是基本类型，兼容转换
                if (TypeUtil.canTransfer(fieldType, targetField.getType())) {
                    ReflectUtil.setStaticFiledValue(targetField, TypeUtil.basicTransfer(value, targetField.getType()));
                } else {
                    LogUtil.debug("变量[{}]的类型[{}]与目标变量的类型[{}]不一致，跳过拷贝", fieldName, fieldType, targetField.getType());
                }
                continue;
            }

            Class[] genericTypes = getFieldGenericTypes(field, fieldType);
            Object copiedValue = deepCopyValue(value, cl, genericTypes);
            ReflectUtil.setStaticFiledValue(targetField, copiedValue);
        }
    }

    private static Class[] getFieldGenericTypes(Field field, Class<?> fieldType) {
        Class[] genericTypes = null;
        if (Collection.class.isAssignableFrom(fieldType)) {
            Class genericType = ReflectUtil.getCollectionFieldGenericType(field);
            if (genericType != null) {
                genericTypes = new Class[] { genericType };
            }
        } else if (Map.class.isAssignableFrom(fieldType)) {
            genericTypes = ReflectUtil.getMapFieldGenericType(field);
        }
        return genericTypes;
    }

    /**
     * 深度拷贝传入对象的数据并返回
     *
     * @param value 对象
     * @param cl 生成的目标对象使用的类加载器；用于模块热更新，外置模块热更新后，即使是完全相同的类，热更前后使用的类加载器也不同
     * @param genericTypes 如果对象有泛型，传入泛型信息
     * @return 返回深度拷贝的数据对象
     */
    public static Object deepCopyValue(Object value, ClassLoader cl, Class... genericTypes) {
        Object result;
        Class<?> valueType;
        if (value == null) {
            result = null;
        } else if ((valueType = value.getClass()).isArray()) {
            result = deepCopyArray((Object[]) value, cl);
        } else if (TypeUtil.isGeneralizedSetsType(valueType)) {
            result = deepCopyGeneralizedSetsData(value, cl, genericTypes);
        } else if (TypeUtil.isNormalType(valueType) || TypeHelper.notModuleType(valueType)) {
            result = value;
        } else {
            // 外置模块中的类对象，深度拷贝
            Class<?> vClass = ClassUtil.getClass(cl, valueType.getName());
            result = buildAndCopyBean(vClass, value);
        }
        return result;
    }

    public static Object[] deepCopyArray(Object[] array, ClassLoader cl) {
        if (array.length == 0) {
            return new Object[0];
        }

        Object[] result;
        Class<?> arrayType = array.getClass();
        Class<?> arrayComponentType = arrayType.getComponentType();
        // 数组的原型
        Class<?> protoType = arrayComponentType;
        while (protoType.isArray()) {
            protoType = protoType.getComponentType();
        }

        // 如果数组的原型为集合或外置模块中的类型，需要进行纯数据的深拷贝
        if (TypeUtil.isGeneralizedSetsType(protoType) || TypeHelper.isModuleInnerType(protoType)) {
            Object[] arr = (Object[]) Array.newInstance(arrayComponentType, array.length);

            // 如果是多维数组，递归执行
            if (arrayComponentType.isArray()) {
                for (int i = 0; i < array.length; i++) {
                    arr[i] = deepCopyArray((Object[]) array[i], cl);
                }
            } else {
                Class<?> moduleType = null;
                boolean isModuleType = TypeHelper.isModuleInnerType(protoType);
                if (isModuleType) {
                    moduleType = ClassUtil.getClass(cl, arrayComponentType.getName());
                }

                for (int i = 0; i < array.length; i++) {
                    Object object = array[i];
                    if (isModuleType) {
                        arr[i] = buildAndCopyBean(moduleType, object);
                    } else {
                        arr[i] = deepCopyGeneralizedSetsData(object, cl);
                    }
                }
            }

            result = arr;
        } else {
            // 数组元素为常见类型或非外置模块中的类，不管是否是多维数组，直接拷贝
            result = array;
        }
        return result;
    }

    /**
     * 深度拷贝集合（广义集合，包括Collection和Map）中的数据并返回
     *
     * @param value 集合对象
     * @param cl 生成的目标对象使用的类加载器；用于模块热更新，外置模块热更新后，即使是完全相同的类，热更前后使用的类加载器也不同
     * @param genericTypes 如果有泛型信息，传入泛型信息
     * @return 返回深度拷贝后的集合
     */
    public static Object deepCopyGeneralizedSetsData(Object value, ClassLoader cl, Class... genericTypes) {
        Object result;
        if (value instanceof Collection) {
            Collection<Object> collection = (Collection<Object>) value;
            result = deepCopyCollection(collection, cl,
                    CollectionUtil.isNotEmpty(genericTypes) ? genericTypes[0] : null);
        } else {
            Map<Object, Object> map = (Map<Object, Object>) value;
            result = deepCopyMap(map, cl, genericTypes);
        }
        return result;
    }

    public static <K, V> Map<K, V> deepCopyMap(Map<?, ?> sourceMap, ClassLoader cl, Class... genericTypes) {
        if (CollectionUtil.isEmpty(sourceMap) || canDirectCopy(genericTypes, 2)) {
            return (Map<K, V>) sourceMap;
        }

        K k;
        V v;
        Map<K, V> targetMap = newMap(sourceMap);
        for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
            k = (K) deepCopyValue(entry.getKey(), cl);
            v = (V) deepCopyValue(entry.getValue(), cl);
            targetMap.put(k, v);
        }
        return targetMap;
    }

    public static Collection<Object> deepCopyCollection(Collection<Object> sourceCollect, ClassLoader cl,
            Class genericType) {
        if (CollectionUtil.isEmpty(sourceCollect) || canDirectCopy(genericType)) {
            return sourceCollect;
        }

        Collection<Object> targetCollect = newCollection(sourceCollect);
        for (Object o : sourceCollect) {
            targetCollect.add(deepCopyValue(o, cl));
        }
        return targetCollect;
    }

    private static <T> T buildAndCopyBean(Class<T> beanType, Object object) {
        T t = ReflectUtil.newInstance(beanType);
        // 深度拷贝
        return deepCopy(object, t);
    }

    private static boolean canDirectCopy(Class[] types, int count) {
        if (CollectionUtil.isNotEmpty(types) && types.length >= count) {
            for (int i = 0; i < count; i++) {
                if (!canDirectCopy(types[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean canDirectCopy(Class type) {
        // 非数组或集合类型且不是外置模块中的类型，才允许直接拷贝
        return type != null && !type.isArray() && !TypeUtil.isGeneralizedSetsType(type) && TypeHelper
                .notModuleType(type);
    }

    private static Collection<Object> newCollection(Collection<?> sourceCollect) {
        int size = sourceCollect.size();
        Class<? extends Collection> collectionType = sourceCollect.getClass();

        Collection<Object> newCollect;
        if (sourceCollect instanceof Queue) {
            // 队列的容量为最大容量，先尝试无参构造创建新对象（无界队列）
            newCollect = ReflectUtil.newInstance(collectionType);
            if (newCollect == null) {
                // 如果调用无参构造创建对象失败，尝试使用带初始容量为参数的构造器，默认为最大容量
                newCollect = ReflectUtil.newInstanceByType(collectionType, new Integer[] { Integer.MAX_VALUE },
                        new Class<?>[] { int.class });
            }
        } else {
            // 尝试使用带初始容量为参数的构造器
            newCollect = ReflectUtil
                    .newInstanceByType(collectionType, new Integer[] { size }, new Class<?>[] { int.class });
            if (newCollect == null) {
                // 如果调用带参构造创建对象失败，尝试无参构造创建新对象
                newCollect = ReflectUtil.newInstance(collectionType);
            }
        }

        if (newCollect != null) {
            return newCollect;
        }

        if (sourceCollect instanceof List) {
            return new ArrayList<>(sourceCollect.size());
        } else if (sourceCollect instanceof Set) {
            return new HashSet<>(sourceCollect.size());
        } else {
            return new LinkedBlockingQueue<>();
        }
    }

    private static <K, V> Map<K, V> newMap(Map<?, ?> sourceMap) {
        int size = sourceMap.size();
        Class<? extends Map> sourceMapType = sourceMap.getClass();

        // 尝试使用带初始容量为参数的构造器
        Map<K, V> newMap = ReflectUtil
                .newInstanceByType(sourceMapType, new Integer[] { size }, new Class<?>[] { int.class });
        if (newMap == null) {
            // 如果调用带参构造创建对象失败，尝试无参构造创建新对象
            newMap = ReflectUtil.newInstance(sourceMapType);
        }

        if (newMap != null) {
            return newMap;
        }

        return new HashMap<>(size);
    }
}