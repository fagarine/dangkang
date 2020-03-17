package cn.laoshini.dk.generator.name;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author fagarine
 */
@Getter
@Setter
@ToString
class ChineseCharacter {

    private int id;

    /**
     * 汉字
     */
    private String character;

    /**
     * 拼音
     */
    private String spelling;

    /**
     * 笔画数
     */
    private int strokes;

    /**
     * 等级，在名字中越常见的字等级越高
     */
    private int level;

    /**
     * 随机权重
     */
    private int weight;
}
