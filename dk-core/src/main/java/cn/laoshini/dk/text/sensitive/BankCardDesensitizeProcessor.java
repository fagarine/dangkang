package cn.laoshini.dk.text.sensitive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.util.StringUtil;

/**
 * 银行卡号脱敏（默认为处理银联卡号（13位或19位），显示前六位，后一位，其他用星号隐藏）
 *
 * @author fagarine
 */
@FunctionVariousWays(value = "bankCard", singleton = false)
public class BankCardDesensitizeProcessor extends AbstractDesensitizeProcessor {

    /**
     * 银联卡正则表达式，62开头，13位或19位
     */
    private static final Pattern UNION_PAY_CARD_PATTERN = Pattern.compile("^62([\\d]{11}|[\\d]{17})$");

    private int leftShow = 6;

    private int rightShow = 1;

    @Override
    protected String defaultMask(String text) {
        if (StringUtil.isEmptyString(text)) {
            return text;
        }

        if (pattern == null) {
            pattern = UNION_PAY_CARD_PATTERN;
        }

        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            char[] chars = text.toCharArray();
            for (int i = leftShow; i < chars.length - rightShow; i++) {
                chars[i] = '*';
            }
            return new String(chars);
        }

        return text;
    }

    /**
     * 设置银行卡号前后显示（不屏蔽）的长度，默认为前6位后1位
     *
     * @param leftShow 前部长度
     * @param rightShow 后部长度
     */
    public void setMaskLength(int leftShow, int rightShow) {
        this.leftShow = leftShow;
        this.rightShow = rightShow;
    }
}
