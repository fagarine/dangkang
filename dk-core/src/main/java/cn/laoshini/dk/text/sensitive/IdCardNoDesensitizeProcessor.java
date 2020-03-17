package cn.laoshini.dk.text.sensitive;

import java.util.regex.Pattern;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.util.StringUtil;

/**
 * 身份证号脱敏（默认隐藏出生年月日信息）
 *
 * @author fagarine
 */
@FunctionVariousWays(value = "idCardNo", singleton = false)
public class IdCardNoDesensitizeProcessor extends AbstractDesensitizeProcessor {

    private static final int ID_NUMBER_LENGTH_15 = 15;

    private static final int ID_NUMBER_LENGTH_18 = 18;

    private static final Pattern DEFAULT_PATTERN_15 = Pattern.compile("(?<=\\d{6})\\d(?=\\d{3})");

    private static final Pattern DEFAULT_PATTERN_18 = Pattern.compile("(?<=\\d{6})\\d(?=\\d{3}\\w)");

    private static final String ID_NUMBER_REPLACEMENT = "*";

    @Override
    public String defaultMask(String idCardNo) {
        if (StringUtil.isEmptyString(idCardNo)) {
            return idCardNo;
        }

        int len = idCardNo.trim().length();
        if (len != ID_NUMBER_LENGTH_15 && len != ID_NUMBER_LENGTH_18) {
            return idCardNo;
        }

        String replacement = this.replacement;
        Pattern pattern = this.pattern;
        if (pattern == null) {
            pattern = (len == ID_NUMBER_LENGTH_15 ? DEFAULT_PATTERN_15 : DEFAULT_PATTERN_18);
            replacement = ID_NUMBER_REPLACEMENT;
        }

        return pattern.matcher(idCardNo).replaceAll(replacement);
    }
}
