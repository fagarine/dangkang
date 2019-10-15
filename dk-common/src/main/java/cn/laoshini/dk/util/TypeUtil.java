package cn.laoshini.dk.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.laoshini.dk.exception.BusinessException;

/**
 * @author fagarine
 */
public class TypeUtil {
    private TypeUtil() {
    }

    public static final List<Class<?>> PRIMITIVE_TYPES = Arrays
            .asList(boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class,
                    double.class);

    public static final List<Class<?>> PRIMITIVE_BOX_TYPES = Arrays
            .asList(Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class,
                    Double.class);

    public static final List<Class<?>> NORMAL_TYPES = Arrays
            .asList(Class.class, BigDecimal.class, BigInteger.class, Date.class, java.sql.Date.class, Timestamp.class);

    /**
     * 是否是基本类型或基本类型的封装类型
     *
     * @param type 用来比较的类型
     * @return 返回比较结果
     */
    public static boolean isPrimitiveType(Class<?> type) {
        if (type == null) {
            return false;
        }
        return PRIMITIVE_TYPES.contains(type) || PRIMITIVE_BOX_TYPES.contains(type);
    }

    public static boolean isBasicType(Class<?> type) {
        return isPrimitiveType(type) || String.class.equals(type);
    }

    public static boolean isNormalType(Class<?> type) {
        return isBasicType(type) || NORMAL_TYPES.contains(type);
    }

    /**
     * 是否是广义上的集合类型，包括Collection和Map
     *
     * @param type 类型
     * @return 返回判断结果
     */
    public static boolean isGeneralizedSetsType(Class<?> type) {
        return Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
    }

    /**
     * 是否是相同或相似的类型
     *
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 返回判断结果
     */
    public static boolean isSimilar(String sourceType, Class<?> targetType) {
        if (targetType == null) {
            return sourceType == null;
        }

        if (targetType.getName().equals(sourceType)) {
            return true;
        }

        Class<?> type = ClassUtil.getClass(targetType.getClassLoader(), sourceType);
        return type != null && targetType.isAssignableFrom(type) && canTransfer(type, targetType);
    }

    /**
     * 是否可转换为目标类型
     *
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 返回判断结果
     */
    public static boolean canTransfer(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == null) {
            return targetType == null;
        }

        if (sourceType.equals(targetType) || targetType.isAssignableFrom(sourceType)) {
            return true;
        }

        if (PRIMITIVE_TYPES.contains(sourceType)) {
            return PRIMITIVE_TYPES.indexOf(sourceType) == PRIMITIVE_BOX_TYPES.indexOf(targetType);
        } else if (PRIMITIVE_BOX_TYPES.contains(sourceType)) {
            return PRIMITIVE_BOX_TYPES.indexOf(sourceType) == PRIMITIVE_TYPES.indexOf(targetType);
        }

        return false;
    }

    /**
     * 基础类型转换
     *
     * @param source 源数据
     * @param targetType 目标类型
     * @param <T> 目标类型
     * @return 返回转换后的数据
     */
    public static <T> T basicTransfer(Object source, Class<T> targetType) {
        if (source == null && targetType == null) {
            throw new BusinessException("", String.format("源数据[%s]和转换后的类型[%s]不能同时为空", source, targetType));
        }

        if (source == null) {
            if (PRIMITIVE_TYPES.contains(targetType)) {
                return (T) unpackPrimitive(source, targetType);
            } else {
                return null;
            }
        }

        Class<?> sourceType = source.getClass();
        if (sourceType.equals(targetType)) {
            return (T) source;
        }

        if (!canTransfer(sourceType, targetType)) {
            throw new BusinessException("", String.format("无法将[%s]转换为类型[%s]", sourceType, targetType));
        }

        if (PRIMITIVE_TYPES.contains(sourceType)) {
            return (T) source;
        } else if (PRIMITIVE_BOX_TYPES.contains(sourceType) && PRIMITIVE_TYPES.contains(targetType)) {
            return (T) unpackPrimitive(source, targetType);
        } else if (String.class.equals(targetType)) {
            return (T) String.valueOf(source);
        } else {
            return null;
        }
    }

    /**
     * 对基本类型的封装类型拆箱并返回，如果传入的源数据为空，使用对应基本类型的默认值
     *
     * @param value 源数据
     * @param targetType 目标类型
     * @return 返回拆箱后的数据
     */
    public static Object unpackPrimitive(Object value, Class targetType) {
        if (PRIMITIVE_TYPES.contains(targetType)) {
            if (boolean.class.equals(targetType)) {
                return value != null && (boolean) value;
            } else if (byte.class.equals(targetType)) {
                return value != null ? value : (byte) 0;
            } else if (char.class.equals(targetType)) {
                return value != null ? value : ' ';
            } else if (short.class.equals(targetType)) {
                return value != null ? value : (short) 0;
            } else if (int.class.equals(targetType)) {
                return value != null ? value : 0;
            } else if (long.class.equals(targetType)) {
                return value != null ? value : 0L;
            } else if (float.class.equals(targetType)) {
                return value != null ? value : 0f;
            } else if (double.class.equals(targetType)) {
                return value != null ? value : 0D;
            }
        }
        return null;
    }

    /**
     * 将字符串转换为指定的基本类型
     *
     * @param value 字符串数据
     * @param toType 目标类型
     * @return 返回转换后的数据
     */
    public static Object parseBasicTypeString(String value, Class<?> toType) {
        if (!isPrimitiveType(toType)) {
            throw new IllegalArgumentException(String.format("传入类型不是基本数据类型，不能转换, type:%s, value:%s", toType, value));
        }

        if (value == null || value.trim().isEmpty()) {
            return getPrimitiveTypeInitValue(toType);
        }

        if (boolean.class.equals(toType) || Boolean.class.equals(toType)) {
            return Boolean.parseBoolean(value);
        } else if (byte.class.equals(toType) || Byte.class.equals(toType)) {
            return Byte.parseByte(value);
        } else if (char.class.equals(toType) || Character.class.equals(toType)) {
            return value.charAt(0);
        } else if (short.class.equals(toType) || Short.class.equals(toType)) {
            return Short.parseShort(value);
        } else if (int.class.equals(toType) || Integer.class.equals(toType)) {
            return Integer.parseInt(value);
        } else if (long.class.equals(toType) || Long.class.equals(toType)) {
            return Long.parseLong(value);
        } else if (float.class.equals(toType) || Float.class.equals(toType)) {
            return Float.parseFloat(value);
        } else if (double.class.equals(toType) || Double.class.equals(toType)) {
            return Double.parseDouble(value);
        }
        return value;
    }

    public static Object getPrimitiveTypeInitValue(Class<?> toType) {
        if (boolean.class.equals(toType) || Boolean.class.equals(toType)) {
            return false;
        } else if (byte.class.equals(toType) || Byte.class.equals(toType)) {
            return (byte) 0;
        } else if (char.class.equals(toType) || Character.class.equals(toType)) {
            return (char) 0;
        } else if (short.class.equals(toType) || Short.class.equals(toType)) {
            return (short) 0;
        } else if (int.class.equals(toType) || Integer.class.equals(toType)) {
            return 0;
        } else if (long.class.equals(toType) || Long.class.equals(toType)) {
            return 0L;
        } else if (float.class.equals(toType) || Float.class.equals(toType)) {
            return 0F;
        } else if (double.class.equals(toType) || Double.class.equals(toType)) {
            return 0D;
        }
        throw new IllegalArgumentException("传入类型不是基本数据类型:" + toType);
    }
}
