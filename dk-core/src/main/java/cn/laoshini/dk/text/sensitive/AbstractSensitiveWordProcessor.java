package cn.laoshini.dk.text.sensitive;

import java.util.HashMap;
import java.util.Map;

import cn.laoshini.dk.util.StringUtil;

/**
 * @author fagarine
 */
public abstract class AbstractSensitiveWordProcessor implements ISensitiveWordProcessor {

    protected static final char BLANK_CHAR = ' ';

    /**
     * 记录匹配规则，默认使用最严格匹配规则
     */
    protected MatchRule matchRule = MatchRule.STRICT;

    /**
     * 记录敏感词屏蔽策略，默认使用一对一星号屏蔽
     */
    protected ISensitiveMaskPolicy maskPolicy = ISensitiveMaskPolicy.oneToOneStarMaskPolicy();

    /**
     * 记录屏蔽词信息，key: 屏蔽词起始字符, value: 关联字符
     */
    protected volatile Map<Character, Object> wordMap;

    @Override
    public AbstractSensitiveWordProcessor loadSensitiveWords(String words, String separator) {
        if (StringUtil.isEmptyString(words)) {
            return this;
        }

        String sensitiveWords = words.trim();
        initWordMap(sensitiveWords.length());

        if (separator == null || separator.length() == 0) {
            loadSensitiveWord(sensitiveWords);
        } else {
            String[] ss = sensitiveWords.split(separator);
            for (String word : ss) {
                loadSensitiveWord(word);
            }
        }

        return this;
    }

    private synchronized void initWordMap(int estimateCount) {
        if (wordMap == null) {
            wordMap = new HashMap<>(estimateCount);
        }
    }

    protected synchronized void loadSensitiveWord(String word) {
        if (StringUtil.isEmptyString(word)) {
            return;
        }

        Map<Character, Object> map;
        Map<Character, Object> nowMap = wordMap;
        for (char key : word.toCharArray()) {
            if (BLANK_CHAR == key) {
                continue;
            }

            Object value = nowMap.get(key);
            if (value != null) {
                nowMap = (Map<Character, Object>) value;
            } else {
                map = new HashMap<>();
                map.put(BLANK_CHAR, false);
                nowMap.put(key, map);
                nowMap = map;
            }
        }
        nowMap.put(BLANK_CHAR, true);
    }

    @Override
    public boolean containsSensitiveWord(String text) {
        if (StringUtil.isEmptyString(text)) {
            return false;
        }

        Map<Character, Object> nowMap = wordMap;
        for (char key : text.toCharArray()) {
            Object value = nowMap.get(key);
            if (value != null) {
                nowMap = (Map<Character, Object>) value;
                if (Boolean.TRUE.equals(nowMap.get(BLANK_CHAR))) {
                    return true;
                }
            } else {
                if (MatchRule.STRICT.equals(matchRule)) {
                    nowMap = wordMap;
                }
            }
        }
        return false;
    }

    @Override
    public int countSensitiveWord(String text) {
        if (StringUtil.isEmptyString(text)) {
            return 0;
        }

        int count = 0;
        Map<Character, Object> nowMap = wordMap;
        for (char key : text.toCharArray()) {
            Object value = nowMap.get(key);
            if (value != null) {
                nowMap = (Map<Character, Object>) value;
                if (Boolean.TRUE.equals(nowMap.get(BLANK_CHAR))) {
                    count++;
                    nowMap = wordMap;
                }
            } else {
                if (MatchRule.STRICT.equals(matchRule)) {
                    nowMap = wordMap;
                }
            }
        }
        return count;
    }

    @Override
    public String replaceSensitiveWord(String text) {
        if (StringUtil.isEmptyString(text)) {
            return text;
        }

        int beginIndex = -1;
        int copiedIndex = -1;
        int newCharIndex = 0;
        char[] chars = text.toCharArray();
        char[] newChars = new char[chars.length];
        Map<Character, Object> nowMap = wordMap;
        for (int i = 0; i < chars.length; i++) {
            char key = chars[i];
            Object value = nowMap.get(key);
            if (value != null) {
                if (beginIndex < 0) {
                    beginIndex = i;
                    // 拷贝未匹配上的字符
                    if (copiedIndex < i - 1) {
                        int count = i - 1 - copiedIndex;
                        newChars = copyCharsToNewArray(chars, copiedIndex + 1, newChars, newCharIndex, count);
                        newCharIndex += count;
                        copiedIndex = i - 1;
                    }
                }
                nowMap = (Map<Character, Object>) value;
                if (Boolean.TRUE.equals(nowMap.get(BLANK_CHAR))) {
                    // 敏感词匹配成功，替换敏感词相关字符
                    int maskCount = i + 1 - beginIndex;
                    char[] maskChars = maskPolicy.mask(maskCount);
                    if (maskChars != null && maskChars.length > 0) {
                        newChars = copyCharsToNewArray(maskChars, 0, newChars, newCharIndex, maskChars.length);
                        newCharIndex += maskChars.length;
                        copiedIndex = i;
                    }
                    nowMap = wordMap;
                    beginIndex = -1;
                }
            } else {
                if (MatchRule.STRICT.equals(matchRule)) {
                    nowMap = wordMap;
                    if (beginIndex >= 0) {
                        beginIndex = -1;
                    }
                }
            }
        }
        if (copiedIndex < chars.length - 1) {
            int count = chars.length - 1 - copiedIndex;
            newChars = copyCharsToNewArray(chars, copiedIndex + 1, newChars, newCharIndex, count);
            newCharIndex += count;
        }

        return new String(newChars, 0, newCharIndex);
    }

    private char[] copyCharsToNewArray(char[] source, int srcPos, char[] dest, int destPos, int length) {
        // 目标数组长度不够，先扩容
        int destCapacity = destPos + length;
        if (dest.length < destCapacity) {
            char[] chars = new char[destCapacity];
            System.arraycopy(dest, 0, chars, 0, dest.length);
            dest = chars;
        }
        System.arraycopy(source, srcPos, dest, destPos, length);
        return dest;
    }

    @Override
    public void clear() {
        if (wordMap != null) {
            wordMap.clear();
        }
    }

    @Override
    public AbstractSensitiveWordProcessor setMatchRule(MatchRule matchRule) {
        if (matchRule != null) {
            this.matchRule = matchRule;
        }
        return this;
    }

    @Override
    public AbstractSensitiveWordProcessor setMaskPolicy(ISensitiveMaskPolicy maskPolicy) {
        this.maskPolicy = maskPolicy;
        return this;
    }
}
