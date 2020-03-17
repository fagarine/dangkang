package cn.laoshini.dk.register;

import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.exception.BusinessException;

/**
 * Websocket服务器注册器
 *
 * @author fagarine
 */
public final class WebsocketGameServerRegister<S, M> extends GameServerRegisterAdaptor<S, M> {

    /**
     * 帧数据格式类型，默认为文本类型
     */
    private FrameType frameType = FrameType.TEXT;

    public WebsocketGameServerRegister() {
        setProtocol(GameServerProtocolEnum.WEBSOCKET);
    }

    @Override
    public WebsocketGameServerRegister<S, M> setProtocol(GameServerProtocolEnum protocolEnum) {
        if (!GameServerProtocolEnum.WEBSOCKET.equals(protocolEnum)) {
            throw new BusinessException("invalid.protocol.type", "WebsocketGameServerRegister的通信协议只能为WEBSOCKET");
        }
        return (WebsocketGameServerRegister<S, M>) super.setProtocol(protocolEnum);
    }

    @Override
    public WebsocketGameServerRegister<S, M> http() {
        throw new BusinessException("invalid.protocol.type", "WebsocketGameServerRegister的通信协议不能设置为HTTP");
    }

    /**
     * 设置帧数据格式类型，详见：{@link FrameType}
     *
     * @param frameType 帧数据格式类型
     * @return 返回当前对象
     */
    public WebsocketGameServerRegister<S, M> setFrameType(FrameType frameType) {
        this.frameType = frameType;
        return this;
    }

    /**
     * 将帧数据格式设置为二进制数据类型
     *
     * @return 返回当前对象
     */
    public WebsocketGameServerRegister<S, M> binaryFrame() {
        this.frameType = FrameType.BINARY;
        return this;
    }

    /**
     * 将帧数据格式设置为文本类型
     *
     * @return 返回当前对象
     */
    public WebsocketGameServerRegister<S, M> textFrame() {
        this.frameType = FrameType.TEXT;
        return this;
    }

    /**
     * 获取帧数据格式类型
     *
     * @return 帧数据格式类型
     */
    public FrameType frameType() {
        return frameType;
    }

    public boolean isBinaryFrame() {
        return FrameType.BINARY.equals(frameType);
    }

    /**
     * Websocket的帧数据格式类型（正式消息，不包含握手、断开连接等消息）
     */
    public enum FrameType {
        /**
         * 文本
         */
        TEXT,
        /**
         * 二进制数据
         */
        BINARY;
    }
}
