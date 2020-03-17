package cn.laoshini.dk.text.sensitive;

import java.util.regex.Pattern;

import cn.laoshini.dk.util.StringUtil;

/**
 * Email信息脱敏（默认仅显示第一个字母，前缀其他隐藏，用星号代替）
 *
 * @author fagarine
 */
public class EmailDesensitizeProcessor extends AbstractDesensitizeProcessor {

    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("^[A-Za-z0-9\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-\\u4e00-\\u9fa5]+(\\.[a-zA-Z0-9_-\\u4e00-\\u9fa5]+)+$");

    @Override
    protected String defaultMask(String text) {
        if (StringUtil.isEmptyString(text)) {
            return text;
        }

        int index = text.indexOf('@');
        if (index <= 1) {
            return text;
        }

        if (pattern == null) {
            pattern = EMAIL_PATTERN;
        }

        if (!pattern.matcher(text).find()) {
            return text;
        }

        char[] chars = text.toCharArray();
        for (int i = 1; i < index; i++) {
            chars[i] = '*';
        }
        return new String(chars);
    }
}
