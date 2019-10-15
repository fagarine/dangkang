package cn.laoshini.dk.constant;

/**
 * @author fagarine
 */
public enum LogLabel {
    /**
     * 错误日志
     */
    ERROR("error"),
    /**
     * 警告日志
     */
    WARN("warn"),
    /**
     * INFO日志
     */
    INFO("info"),
    /**
     * DEBUG日志
     */
    DEBUG("debug"),
    /**
     * java agent日志
     */
    AGENT("agent"),
    /**
     * session日志
     */
    SESSION("session"),
    /**
     * 客户端发往服务端的消息日志
     */
    C2S("c2s"),
    /**
     * 服务端发往客户端的消息日志
     */
    S2C("s2c"),
    /**
     * 消息日志，不区分上行下行
     */
    MESSAGE("msg"),
    /**
     * 协议处理handler相关日志
     */
    HANDLER("handler"),
    ;

    private String label;

    LogLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
