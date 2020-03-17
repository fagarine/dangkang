package cn.laoshini.dk.util;

import java.util.Collection;

import cn.laoshini.dk.constant.Constants;

/**
 * @author fagarine
 */
public class StringUtil {
    private StringUtil() {
    }

    public static void main(String[] args) {
    }

    public static String notNullToString(Object object) {
        if (object == null) {
            return "";
        }
        return String.valueOf(object);
    }

    /**
     * 顺序拼接所有字符串为一个字符串（不使用分隔符）
     *
     * @param messages 待拼接字符串
     * @return 返回拼接后的字符串
     */
    public static String appendAll(String... messages) {
        return appendAllWithDelimiter(messages, "", false);
    }

    /**
     * 顺序拼接所有数据为字符串（使用传入分隔符分割）
     *
     * @param arr 待拼接数据
     * @param delimiter 分隔符
     * @param skipNull 是否跳过空值
     * @return 返回拼接后的字符串
     */
    public static String appendAllWithDelimiter(Object[] arr, String delimiter, boolean skipNull) {
        if (arr == null || arr.length == 0) {
            return "";
        }

        int temp = 0;
        StringBuilder sb = new StringBuilder();
        for (Object key : arr) {
            if (skipNull) {
                if (key == null) {
                    continue;
                }
            }

            if (temp++ > 0) {
                sb.append(delimiter);
            }
            sb.append(notNullToString(key));
        }
        return sb.toString();
    }

    public static String appendKeys(Object[] keys) {
        return appendAllWithDelimiter(keys, Constants.UNDERLINE, true);
    }

    public static String appendKeys(Object[] keys, String delimiter) {
        return appendAllWithDelimiter(keys, delimiter, true);
    }

    public static String appendSqlCondition(Collection<String> keys) {
        if (CollectionUtil.isEmpty(keys)) {
            return null;
        }

        StringBuilder sb = new StringBuilder("'");
        int count = 0;
        for (String key : keys) {
            if (count++ > 0) {
                sb.append("','");
            }
            sb.append(key);
        }
        return sb.append("'").toString();
    }

    public static boolean isEmptyString(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isNotEmptyString(String str) {
        return !isEmptyString(str);
    }
}
