package cn.laoshini.dk.constant;

/**
 * @author fagarine
 */
public enum RoleStatusEnum {
    /**
     * 正常
     */
    NORMAL((short) 1),
    /**
     * 封号
     */
    LOCKED((short) 10),
    /**
     * 禁止登陆
     */
    FORBIDDEN((short) 20),
    /**
     * 无效
     */
    INVALID((short) 30),
    ;

    public static boolean isValidStatus(short code) {
        return NORMAL.code == code;
    }

    private short code;

    RoleStatusEnum(short code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
