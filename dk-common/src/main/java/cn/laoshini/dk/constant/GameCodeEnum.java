package cn.laoshini.dk.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏消息返回码枚举
 *
 * @author fagarine
 */
public enum GameCodeEnum {

    /**
     * 正确
     */
    OK(200, "OK"),
    NO_RESPONSE(600, "NO RESPONSE"),
    NO_MESSAGE(601, "NO MESSAGE"),
    MESSAGE_ILLEGAL(602, "MESSAGE ILLEGAL"),
    FUNCTION_ID_MISSING(603, "MESSAGE ID METHOD MISSING"),
    MESSAGE_ID_METHOD_ERROR(604, "MESSAGE ID METHOD ERROR"),
    MESSAGE_ID_METHOD_ACCESS(605, "MESSAGE ID METHOD ACCESS LIMIT"),
    MESSAGE_ID_NULL(606, "MESSAGE ID NULL"),
    MESSAGE_INIT_FAIL(607, "MESSAGE INIT FAIL"),
    MESSAGE_ID_DUPLICATE(608, "MESSAGE ID DUPLICATE"),
    MESSAGE_DECODE_ERROR(609, "MESSAGE DECODE ERROR"),
    MESSAGE_ENCODER_ERROR(610, "MESSAGE ENCODER ERROR"),
    NO_HANDLER(701, "NO HANDLER"),
    NOT_HTTP_HANDLER(702, "NOT HTTP HANDLER"),
    SERVER_EXCEPTION(703, "SERVER EXCEPTION"),
    UNKNOWN_ERROR(704, "UNKNOWN ERROR"),
    PARAM_ERROR(705, "PARAM ERROR"),
    MESSAGE_TOO_LARGE(706, "MESSAGE BODY TOO LARGE"),
    UNSUPPORTED_HTTP_PROTOCOL(707, "UNSUPPORTED HTTP PROTOCOL"),
    UNSUPPORTED_TCP_PROTOCOL(708, "UNSUPPORTED TCP PROTOCOL"),
    LOGIN_NAME_ERROR(801, "LOGIN NAME ERROR"),
    LOGIN_NAME_DUPLICATED(802, "LOGIN NAME DUPLICATED"),
    PLAYER_NOT_FOUND(803, "PLAYER NOT FOUND"),

    ;

    private static Map<Integer, GameCodeEnum> codeMap = new HashMap<>(values().length);

    static {
        for (GameCodeEnum codeEnum : values()) {
            codeMap.put(codeEnum.getCode(), codeEnum);
        }
    }

    private int code;
    private String desc;

    GameCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static GameCodeEnum codeOf(int code) {
        return codeMap.get(code);
    }

    public static String getDesc(int code) {
        GameCodeEnum codeEnum = codeOf(code);
        return codeEnum == null ? "" : codeEnum.getDesc();
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
