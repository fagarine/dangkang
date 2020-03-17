package cn.laoshini.dk.text.sensitive;

import cn.laoshini.dk.exception.BusinessException;

/**
 * 敏感信息屏蔽策略定义接口
 *
 * @author fagarine
 */
@FunctionalInterface
public interface ISensitiveMaskPolicy {

    char STAR = '*';

    /**
     * 创建并返回一个敏感信息屏蔽策略，该策略为：一个待屏蔽字符对应一个本方法传入的字符
     *
     * @param maskChar 屏蔽后显示字符
     * @return 返回一对一字符屏蔽策略
     */
    static ISensitiveMaskPolicy oneToOneMaskPolicy(char maskChar) {
        return count -> {
            if (count < 0) {
                throw new BusinessException("char.count.invalid", "屏蔽字符数不能是负数:" + count);
            }
            char[] chars = new char[count];
            for (int i = 0; i < count; i++) {
                chars[i] = maskChar;
            }
            return chars;
        };
    }

    /**
     * 创建并返回一个敏感信息屏蔽策略，该策略屏蔽结果，一个字符对应一个星号(*)
     *
     * @return 返回一对一星号屏蔽策略
     */
    static ISensitiveMaskPolicy oneToOneStarMaskPolicy() {
        return oneToOneMaskPolicy(STAR);
    }

    /**
     * 创建并返回一个敏感信息屏蔽策略，该策略为：不管待屏蔽的字符数量有多少，都会使用本方法传入的固定长度的固定字符代替
     *
     * @param fixedStarCount 屏蔽信息固定长度
     * @param maskChar 屏蔽后显示字符
     * @return 返回固定长度星号屏蔽策略
     */
    static ISensitiveMaskPolicy fixedCharMaskPolicy(int fixedStarCount, char maskChar) {
        return count -> {
            if (fixedStarCount < 0) {
                throw new BusinessException("char.count.invalid", "固定屏蔽字符数不能是负数:" + fixedStarCount);
            }
            char[] chars = new char[fixedStarCount];
            for (int i = 0; i < fixedStarCount; i++) {
                chars[i] = maskChar;
            }
            return chars;
        };
    }

    /**
     * 创建并返回一个敏感信息屏蔽策略，该策略不管待屏蔽的字符数量有多少，都会使用本方法传入的固定长度的星号代替
     *
     * @param fixedStarCount 星号固定长度
     * @return 返回固定长度星号屏蔽策略
     */
    static ISensitiveMaskPolicy fixedStarMaskPolicy(int fixedStarCount) {
        return fixedCharMaskPolicy(fixedStarCount, STAR);
    }

    /**
     * 根据传入的待屏蔽字符数，返回用来代替源字符串的屏蔽字符数组
     *
     * @param charCount 待屏蔽字符数
     * @return 返回屏蔽对应字符的字符数组
     */
    char[] mask(int charCount);

}
