package cn.laoshini.dk.constant;

/**
 * 数据访问内部实现相关常量定义
 *
 * @author fagarine
 */
public class DaConstant {
    private DaConstant() {
    }

    /**
     * 关系数据库，记录id自增器序列值的表
     */
    public static final String ID_INCREMENTER_TABLE = "id_incrementer";

    /**
     * 键值对数据库，记录id自增器序列值的前缀
     */
    public static final String ID_INCREMENTER_PREFIX = "ID:INCREMENTER:";
}
