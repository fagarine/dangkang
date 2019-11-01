package cn.laoshini.dk.server;

import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.domain.GameServerConfig;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.server.impl.CustomNettyTcpGameServer;
import cn.laoshini.dk.server.impl.JsonNettyTcpGameServer;
import cn.laoshini.dk.server.impl.ProtobufNettyTcpGameServer;

/**
 * @author fagarine
 */
public class NettyServerFactory {
    private NettyServerFactory() {
    }

    public static <T> AbstractNettyTcpGameServer<T> buildTcpServerByGameConfig(GameServerConfig config) {
        if (GameServerProtocolEnum.TCP.equals(config.getProtocol())) {
            switch (config.getFormat()) {
                case CUSTOM:
                    return (AbstractNettyTcpGameServer<T>) new CustomNettyTcpGameServer(config);

                case JSON:
                    return new JsonNettyTcpGameServer(config);

                case PROTOBUF:
                    return (AbstractNettyTcpGameServer<T>) new ProtobufNettyTcpGameServer(config);

                default:
                    throw new BusinessException("format.not.supported", "不支持的消息格式: " + config.getFormat());
            }
        } else {
            throw new BusinessException("format.not.supported", "不支持的协议类型: " + config.getProtocol());
        }
    }
}
