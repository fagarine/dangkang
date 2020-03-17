package cn.laoshini.dk.text.sensitive;

import java.io.File;
import java.io.IOException;

import cn.laoshini.dk.annotation.ConfigurableFunction;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.util.FileUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * 敏感词处理器功能定义接口
 *
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.sensitive.processor")
public interface ISensitiveWordProcessor {

    /**
     * 从文件中加载敏感词数据
     *
     * @param file 记录敏感词的文件
     * @return 返回当前对象，用于fluent编程
     */
    default ISensitiveWordProcessor loadSensitiveWordsFromFile(File file) {
        try {
            String words = FileUtil.readFileToString(file);
            if (StringUtil.isNotEmptyString(words)) {
                // 文件中的敏感词默认以换行符分割，即一行一个敏感词
                return loadSensitiveWords(words, Constants.LINE_SEPARATOR);
            }
        } catch (IOException e) {
            throw new BusinessException("sensitive.file.error", "读取敏感词文件出错:" + file);
        }
        return this;
    }

    /**
     * 加载敏感词数据
     *
     * @param words 敏感词数据
     * @param separator 分割敏感词的符号
     * @return 返回当前对象，用于fluent编程
     */
    ISensitiveWordProcessor loadSensitiveWords(String words, String separator);

    /**
     * 检验传入字符串是否包含敏感词信息，并返回检验结果（调用该方法需要保证敏感词数据已加载成功）
     *
     * @param text 待检验的字符串
     * @return 如果包含敏感词，返回true
     */
    default boolean containsSensitiveWord(String text) {
        return countSensitiveWord(text) > 0;
    }

    /**
     * 计算传入字符串中，包含有几个敏感词（调用该方法需要保证敏感词数据已加载成功）
     *
     * @param text 字符串
     * @return 返回传入字符串包含敏感词数量
     */
    int countSensitiveWord(String text);

    /**
     * 屏蔽传入字符串中的敏感词信息，并返回处理后的字符串
     *
     * @param text 待处理的字符串
     * @return 返回处理后的字符串
     */
    String replaceSensitiveWord(String text);

    /**
     * 清空已加载的敏感词数据
     */
    void clear();

    /**
     * 设置敏感词匹配规则
     *
     * @param matchRule 敏感词匹配规则
     * @return 返回当前对象，用于fluent编程
     */
    ISensitiveWordProcessor setMatchRule(MatchRule matchRule);

    /**
     * 设置敏感词屏蔽策略
     *
     * @param maskPolicy 敏感词屏蔽策略
     * @return 返回当前对象，用于fluent编程
     */
    ISensitiveWordProcessor setMaskPolicy(ISensitiveMaskPolicy maskPolicy);

    /**
     * 使用一个字符对应一个星号(*)的屏蔽策略
     *
     * @return 返回当前对象，用于fluent编程
     */
    default ISensitiveWordProcessor setOneToOneStarMaskPolicy() {
        return setMaskPolicy(ISensitiveMaskPolicy.oneToOneStarMaskPolicy());
    }

    /**
     * 使用本方法传入的固定长度的星号(*)的屏蔽策略
     *
     * @param fixedStarCount 星号固定长度
     * @return 返回当前对象，用于fluent编程
     */
    default ISensitiveWordProcessor setFixedStarMaskPolicy(int fixedStarCount) {
        return setMaskPolicy(ISensitiveMaskPolicy.fixedStarMaskPolicy(fixedStarCount));
    }

    /**
     * 扩展功能：查找传入字符串包含的第一个敏感词的索引，如果字符串中不包含敏感词，返回-1（该方法依赖实现类，不要求实现类一定实现）
     *
     * @param text 字符串
     * @return 如果字符串中不包含敏感词，返回-1，否则返回第一个敏感词的索引
     */
    default int indexOf(String text) {
        return -1;
    }

    /**
     * 敏感词匹配规则枚举
     */
    enum MatchRule {
        /**
         * 完全一致的，完全匹配完整的敏感词
         */
        STRICT,
        /**
         * 包含完整的敏感词，包括将敏感词用其他字符间隔的形式，都算匹配成功（这种匹配规则可能导致误判）
         */
        INCLUDE,
        ;
    }
}
