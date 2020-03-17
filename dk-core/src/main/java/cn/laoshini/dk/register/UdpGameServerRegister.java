package cn.laoshini.dk.register;

import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.exception.BusinessException;

/**
 * UDP服务器注册器
 *
 * @author fagarine
 */
public final class UdpGameServerRegister<S, M> extends GameServerRegisterAdaptor<S, M> {

    /**
     * 是否创建会话对象，因为UDP面向无连接，一般不需要创建会话对象
     */
    private boolean buildSession;

    public UdpGameServerRegister() {
        setProtocol(GameServerProtocolEnum.UDP);
    }

    @Override
    public final UdpGameServerRegister<S, M> setProtocol(GameServerProtocolEnum protocolEnum) {
        if (!GameServerProtocolEnum.UDP.equals(protocolEnum)) {
            throw new BusinessException("invalid.protocol.type", "UdpGameServerRegister的通信协议只能为WEBSOCKET");
        }
        return (UdpGameServerRegister<S, M>) super.setProtocol(protocolEnum);
    }

    @Override
    public final UdpGameServerRegister<S, M> http() {
        throw new BusinessException("invalid.protocol.type", "UdpGameServerRegister的通信协议不能设置为HTTP");
    }

    /**
     * 设置为创建会话对象模式
     *
     * @return 返回当前对象
     */
    public UdpGameServerRegister<S, M> buildSession() {
        this.buildSession = true;
        return this;
    }

    /**
     * 是否需要创建会话对象
     *
     * @return 返回是否需要创建会话对象
     */
    public boolean isBuildSession() {
        return buildSession;
    }
}
