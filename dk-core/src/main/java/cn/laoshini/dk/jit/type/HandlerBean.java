package cn.laoshini.dk.jit.type;

import cn.laoshini.dk.constant.GameServerProtocolEnum;

/**
 * @author fagarine
 */
public class HandlerBean extends CompositeBean {

    /**
     * 接收消息id，也是handler将会处理的协议号
     */
    private int messageId;

    /**
     * 是否允许玩家没有登录的情况下，发送该消息，默认为false
     */
    private boolean allowGuestRequest;

    /**
     * 消息是否需要按到达顺序执行，默认为true，即需要按顺序执行
     */
    private boolean sequential = true;

    /**
     * 游戏服通信协议类型，默认为TCP
     */
    private String protocol = GameServerProtocolEnum.TCP.name();

    /**
     * 进入消息的消息体类型（详细类型名称，包括泛型信息）
     */
    private String dataType;

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public boolean isAllowGuestRequest() {
        return allowGuestRequest;
    }

    public void setAllowGuestRequest(boolean allowGuestRequest) {
        this.allowGuestRequest = allowGuestRequest;
    }

    public boolean isSequential() {
        return sequential;
    }

    public void setSequential(boolean sequential) {
        this.sequential = sequential;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
