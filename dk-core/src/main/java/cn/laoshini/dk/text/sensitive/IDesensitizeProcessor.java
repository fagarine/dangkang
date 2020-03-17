package cn.laoshini.dk.text.sensitive;

import cn.laoshini.dk.annotation.ConfigurableFunction;

/**
 * 字符串脱敏处理器定义接口
 *
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.desensitize.processor")
public interface IDesensitizeProcessor {

    /**
     * 屏蔽传入字符串中的敏感信息
     *
     * @param text 字符串
     * @return 返回脱敏后的字符串
     */
    String mask(String text);

    /**
     * 使用传入的正则表达式和替换字符串，通过正则匹配模式处理。注意：是否使用正则匹配依赖于实现类，如果实现类中不支持正则，则该方法无效
     *
     * @param regex 正则匹配表达式
     * @param replacement 脱敏数据替换字符串（或表达式）
     */
    void useRegex(String regex, String replacement);

}
