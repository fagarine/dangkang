package cn.laoshini.dk.constant;

/**
 * Class用途分类枚举，按用途对Class分类
 *
 * @author fagarine
 */
public enum UseTypeEnum {
    /**
     * POJO类、普通实例类
     */
    ORDINARY,

    /**
     * 静态工具类
     */
    UTIL,

    /**
     * 自定义格式消息DTO类
     */
    DTO,

    /**
     * Spring托管类
     */
    SPRING_BEAN,

    /**
     * 资源持有者
     */
    HOLDER,

    ;
}
