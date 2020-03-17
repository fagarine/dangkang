package cn.laoshini.dk.generator.id;

/**
 * id自增器内部实现相关常量定义
 *
 * @author fagarine
 */
public class IdIncrementerConstant {
    /**
     * 关系数据库，记录id自增器序列值的表
     */
    public static final String ID_INCREMENTER_TABLE = "id_incrementer";
    /**
     * 键值对数据库，记录id自增器序列值的前缀
     */
    public static final String ID_INCREMENTER_PREFIX = "ID:INCREMENTER:";
    /**
     * 关系数据库，单次id缓存长度
     */
    public static final int RELATION_DB_CACHE_SIZE = 100;
    /**
     * 键值对数据库，单次id缓存长度
     */
    public static final int PAIR_DB_CACHE_SIZE = 100;

    private IdIncrementerConstant() {
    }
}
