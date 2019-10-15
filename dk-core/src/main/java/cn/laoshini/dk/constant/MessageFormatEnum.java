package cn.laoshini.dk.constant;

/**
 * 消息格式类型枚举
 *
 * @author fagarine
 */
public enum MessageFormatEnum {

    /**
     * json，对应普通的JavaBean，比如DTO、VO等
     */
    JSON,
    /**
     * protobuf
     */
    PROTOBUF,
    /**
     * 自定义消息格式
     */
    CUSTOM,
    ;
}
