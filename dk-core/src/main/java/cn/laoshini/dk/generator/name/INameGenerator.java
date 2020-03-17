package cn.laoshini.dk.generator.name;

import java.util.List;

import cn.laoshini.dk.annotation.ConfigurableFunction;

/**
 * 游戏角色名称生成器（实现类实现具体的生成规则和格式）
 *
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.name.generator", description = "游戏角色名称生成器")
public interface INameGenerator {

    /**
     * 批量生成名称时，单批次最大生成名称的数量限制
     */
    int BATCH_COUNT_LIMIT = 1000;

    /**
     * 生成一个新的名称并返回
     *
     * @param surname 传入姓氏，传入null则随机姓氏
     * @param length 名称最大长度（包含姓氏），如果传入非正整数，表示长度随机
     * @return 返回名称
     */
    String newName(String surname, int length);

    /**
     * 生成一个新的名称并返回，名称长度随机
     *
     * @param surname 传入姓氏，传入null则随机姓氏
     * @return 返回名称
     */
    default String newName(String surname) {
        return newName(surname, 0);
    }

    /**
     * 生成一个新的名称并返回，姓氏随机
     *
     * @param length 名称最大长度（包含姓氏），如果传入非正整数，表示长度随机
     * @return 返回名称
     */
    default String newName(int length) {
        return newName(null, length);
    }

    /**
     * 生成一个新的名称并返回，姓氏和名称长度随机
     *
     * @return 返回名称
     */
    default String newName() {
        return newName(null, 0);
    }

    /**
     * 批量生成名称
     *
     * @param surname 传入姓氏，传入null则随机姓氏
     * @param nameLen 名称最大长度（包含姓氏），如果传入非正整数，表示长度随机
     * @param count 生成数量
     * @return 返回结果，该方法不会返回null
     */
    List<String> batchName(String surname, int nameLen, int count);

    /**
     * 批量生成名称
     *
     * @param surname 传入姓氏，传入null则随机姓氏
     * @param count 生成数量
     * @return 返回结果，该方法不会返回null
     */
    default List<String> batchName(String surname, int count) {
        return batchName(surname, 0, count);
    }

    /**
     * 批量生成名称
     *
     * @param nameLen 名称最大长度（包含姓氏），如果传入非正整数，表示长度随机
     * @param count 生成数量
     * @return 返回结果，该方法不会返回null
     */
    default List<String> batchName(int nameLen, int count) {
        return batchName(null, nameLen, count);
    }

    /**
     * 批量生成名称
     *
     * @param count 生成数量
     * @return 返回结果，该方法不会返回null
     */
    default List<String> batchName(int count) {
        return batchName(null, 0, count);
    }

    /**
     * 规范单次批量生成名称的数量
     *
     * @param batchCount 传入数量
     * @return 返回允许的数量
     */
    default int batchCount(int batchCount) {
        if (batchCount <= 0 || batchCount >= BATCH_COUNT_LIMIT) {
            return BATCH_COUNT_LIMIT;
        }
        return batchCount;
    }

    /**
     * 设置名字（不包含姓氏）的最大长度，用于随机名字时限制最大长度
     *
     * @param limit 名字最大长度
     */
    void setNameLengthLimit(int limit);

    /**
     * 获取名字（不包含姓氏）的最大长度
     *
     * @return 返回结果
     */
    int nameLengthLimit();

    /**
     * 设置名字之间的连接符/分隔符，生成外国人名字时可能需要用到连接符
     * <p>
     * 该方法是否有效，依赖于实现类是否实现了该功能
     * </p>
     *
     * @param nameSeparator 连接符
     */
    default void setNameSeparator(String nameSeparator) {
    }

    /**
     * 设置名称生成规则的等级，该功能依赖于实现类是否提供实现
     *
     * @param level 等级，等级越高可能会越耗性能，是否如此同样依赖于实现方式
     */
    default void setLevel(Level level) {
    }

    /**
     * 名字生成规则等级接口
     */
    interface Level {
    }
}
