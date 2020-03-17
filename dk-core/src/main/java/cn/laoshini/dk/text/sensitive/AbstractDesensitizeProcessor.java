package cn.laoshini.dk.text.sensitive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.laoshini.dk.util.StringUtil;

/**
 * 数据脱敏抽象类，支持自定义正则
 *
 * @author fagarine
 */
public abstract class AbstractDesensitizeProcessor implements IDesensitizeProcessor {

    protected Pattern pattern;

    protected String replacement;

    /**
     * 记录是否使用用户自定义的正则表达式
     */
    private boolean customRegex;

    @Override
    public void useRegex(String regex, String replacement) {
        this.pattern = Pattern.compile(regex);
        this.replacement = replacement;
        this.customRegex = true;
    }

    @Override
    public String mask(String text) {
        if (StringUtil.isEmptyString(text)) {
            return text;
        }

        if (customRegex) {
            // 使用用户自定义实现
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.replaceAll(replacement);
            } else {
                return text;
            }
        }

        // 使用默认实现
        return defaultMask(text);
    }

    /**
     * 屏蔽敏感信息的默认实现
     *
     * @param text 字符串
     * @return 返回处理后的字符串
     */
    protected abstract String defaultMask(String text);
}
