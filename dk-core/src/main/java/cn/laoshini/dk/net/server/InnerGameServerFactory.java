package cn.laoshini.dk.net.server;

import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.register.GameServerRegisterAdaptor;

/**
 * @author fagarine
 */
public class InnerGameServerFactory {
    private InnerGameServerFactory() {
    }

    public static <S, M> AbstractInnerGameServer<S, M> newGameServer(
            GameServerRegisterAdaptor<S, M> gameServerRegister) {
        switch (gameServerRegister.protocol()) {
            case TCP:
                return new InnerNettyTcpGameServer<>(gameServerRegister);

            case UDP:
                return new InnerNettyUdpGameServer<>(gameServerRegister);

            case HTTP:
                return new InnerNettyHttpGameServer<>(gameServerRegister);

            default:
                throw new BusinessException("not.supported.protocol", "不支持的游戏服务器通信协议:" + gameServerRegister.protocol());
        }
    }
}
