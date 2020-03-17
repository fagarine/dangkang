package cn.laoshini.dk.generator.name;

import java.util.List;

/**
 * @author fagarine
 */
interface ILevelRandomNameGenerator {

    /**
     * 生成一个新的名称并返回
     *
     * @param surname 传入姓氏，传入null则随机姓氏
     * @param maxLen 名称最大长度（包含姓氏）
     * @return 返回名称
     */
    String newName(String surname, int maxLen);

    /**
     * 批量生成名称
     *
     * @param surname 传入姓氏，传入null则随机姓氏
     * @param maxLen 名称最大长度（包含姓氏）
     * @param count 生成数量
     * @return 返回结果，该方法不会返回null
     */
    List<String> batchName(String surname, int maxLen, int count);

    /**
     * 设置名字（不包含姓氏）的最大长度
     *
     * @param nameLimit 长度限制
     */
    void setNameLimit(int nameLimit);
}
