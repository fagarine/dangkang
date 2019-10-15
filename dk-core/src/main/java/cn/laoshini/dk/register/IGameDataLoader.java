package cn.laoshini.dk.register;

/**
 * 游戏数据加载器
 *
 * @author fagarine
 */
public interface IGameDataLoader {

    /**
     * 获取游戏数据加载器名称
     *
     * @return 该方法不应该返回null，当一个游戏中有多个数据加载器时，应该使用不同的名称
     */
    String name();

    /**
     * 开始执行数据加载逻辑
     */
    void load();

    /**
     * 重新加载数据，一般用于热加载配置数据
     */
    void reload();

    /**
     * 清空数据
     */
    default void clear() {
    }
}
