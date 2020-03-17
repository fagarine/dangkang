package cn.laoshini.dk.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏服状态枚举
 *
 * @author fagarine
 */
public enum GameServerStatus {

    /**
     * 运行中
     */
    RUN(1),

    /**
     * 已停止业务
     */
    PAUSE(2),

    /**
     * 已关闭，未启动
     */
    CLOSE(3),

    ;

    private static final Map<Integer, GameServerStatus> CODE_TO_STATUS = new HashMap<>(values().length);

    static {
        for (GameServerStatus status : values()) {
            CODE_TO_STATUS.put(status.code, status);
        }
    }

    private int code;

    GameServerStatus(int code) {
        this.code = code;
    }

    public static GameServerStatus getByCode(int code) {
        return CODE_TO_STATUS.get(code);
    }

    public int getCode() {
        return code;
    }
}
