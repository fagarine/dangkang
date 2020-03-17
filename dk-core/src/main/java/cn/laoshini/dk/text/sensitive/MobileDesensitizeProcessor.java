package cn.laoshini.dk.text.sensitive;

import java.util.regex.Pattern;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.util.StringUtil;

/**
 * 手机号码脱敏（默认为隐藏中间4位，使用星号(*)屏蔽）
 *
 * @author fagarine
 */
@FunctionVariousWays(value = "mobile", singleton = false)
public class MobileDesensitizeProcessor extends AbstractDesensitizeProcessor {

    private static final int MOBILE_NUMBER_LENGTH = 11;

    private static final String MOBILE_REPLACEMENT = "$1****$2";

    private static final Pattern DEFAULT_PATTERN = Pattern.compile("(\\d{3})\\d{4}(\\d{4})");

    @Override
    public String defaultMask(String mobile) {
        if (StringUtil.isEmptyString(mobile) || mobile.trim().length() != MOBILE_NUMBER_LENGTH) {
            return mobile;
        }

        String replacement = this.replacement;
        if (pattern == null) {
            pattern = DEFAULT_PATTERN;
        }

        if (pattern == DEFAULT_PATTERN) {
            replacement = MOBILE_REPLACEMENT;
        }

        return pattern.matcher(mobile).replaceAll(replacement);
    }
}
