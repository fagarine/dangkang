package cn.laoshini.dk.constant;

/**
 * 游戏模块相关常量
 *
 * @author fagarine
 */
public class GameConstant {
    private GameConstant() {
    }

    /**
     * TCP游戏服默认占用端口
     */
    public static final int DEFAULT_TCP_SERVER_PORT = 9420;

    /**
     * handler执行逻辑的action方法名称
     */
    public static final String HANDLER_ACTION_METHOD = "action";

    /**
     * handler执行逻辑的call方法名称
     */
    public static final String HANDLER_CALL_METHOD = "call";

    /**
     * protobuf和自定义格式消息读取时，记录消息总长度的偏移字节数
     */
    public static final int MESSAGE_LENGTH_OFFSET = 4;

    /**
     * 自定义格式消息读取时，记录校验码的偏移字节数
     */
    public static final int MESSAGE_CHECK_CODE_OFFSET = 4;

    /**
     * 自定义格式消息读取时，记录消息id的偏移字节数
     */
    public static final int MESSAGE_ID_OFFSET = 4;

    /**
     * 消息读取单帧数据（字节数组）最大长度，大小：1M
     */
    public static final int MAX_FRAME_LENGTH = 1048576;

    /**
     * JSON格式消息中，消息id的默认key
     */
    public static final String MESSAGE_ID_KEY = "messageId";

    /**
     * JSON格式消息中，消息内容数据的默认key
     */
    public static final String MESSAGE_DETAIL_KEY = "detail";

    /**
     * JSON格式消息中，消息扩展数据的默认key
     */
    public static final String MESSAGE_EXTENDS_KEY = "params";

    /**
     * JSON格式消息中，消息返回码的默认key
     */
    public static final String MESSAGE_CODE_KEY = "code";

}
