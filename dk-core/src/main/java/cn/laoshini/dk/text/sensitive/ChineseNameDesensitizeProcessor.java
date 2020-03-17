package cn.laoshini.dk.text.sensitive;

import java.util.regex.Pattern;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.util.StringUtil;

/**
 * 中文姓名脱敏（默认只显示姓氏第一个字），默认只支持不超过6个字的姓名，可通过{@link #setNameLength(int)}方法设置最大支持长度
 *
 * @author fagarine
 */
@FunctionVariousWays(value = "cnName", singleton = false)
public class ChineseNameDesensitizeProcessor extends AbstractDesensitizeProcessor {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\u4E00-\\u9FA5]+$");

    /**
     * 汉族中文名的最大长度
     */
    private static final int MAX_LENGTH = 6;

    private int nameLength = MAX_LENGTH;

    @Override
    protected String defaultMask(String text) {
        if (StringUtil.isEmptyString(text) || text.length() == 1 || text.length() > nameLength) {
            return text;
        }

        if (pattern == null) {
            pattern = NAME_PATTERN;
        }

        text = text.trim();
        if (pattern.matcher(text).find()) {
            char[] chars = text.toCharArray();
            for (int i = 1; i < chars.length; i++) {
                chars[i] = '*';
            }
            return new String(chars);
        } else {
            return text;
        }
    }

    /**
     * 设置名称最大长度限制
     *
     * @param maxLength 名称最大长度
     */
    public void setNameLength(int maxLength) {
        this.nameLength = maxLength;
    }

}
