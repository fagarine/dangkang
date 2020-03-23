package cn.laoshini.dk.starter;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.laoshini.dk.condition.ConditionalOnPropertyExists;
import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.AbstractMessage;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.executor.AbstractOrderedWorker;
import cn.laoshini.dk.executor.IOrderedExecutor;
import cn.laoshini.dk.executor.OrderedQueuePoolExecutor;
import cn.laoshini.dk.net.MessageHandlerHolder;
import cn.laoshini.dk.net.codec.JsonNettyMessageDecoder;
import cn.laoshini.dk.net.codec.JsonNettyMessageEncoder;
import cn.laoshini.dk.net.session.AbstractSession;
import cn.laoshini.dk.register.GameServerRegisterAdaptor;
import cn.laoshini.dk.register.IMessageHandlerRegister;
import cn.laoshini.dk.register.IMessageRegister;
import cn.laoshini.dk.register.Registers;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.ReflectUtil;

/**
 * 游戏服配置项自动配置与游戏服务启动支持
 *
 * @author fagarine
 */
@Component
@ConditionalOnPropertyExists(prefix = "dk.game.server", name = { "id", "port" })
public class DkGameServerAutoConfiguration {

    @Value("${dk.game.id:1}")
    private int gameId;
    @Value("${dk.game.name:当康游戏}")
    private String gameName;
    @Value("${dk.game.spring-configs:}")
    private String[] springConfigs;
    @Value("${dk.game.package-prefixes:}")
    private String[] packagePrefixes;
    @Value("${dk.game.message-register:}")
    private Class<IMessageRegister> messageRegister;
    @Value("${dk.game.message-handler-register:}")
    private Class<IMessageHandlerRegister> messageHandlerRegister;
    @Value("${dk.game.message-packages:}")
    private String[] messagePackages;
    @Value("${dk.game.message-handler-packages:}")
    private String[] messageHandlerPackages;
    @Value("${dk.game.server.id}")
    private int serverId;
    @Value("${dk.game.server.name:当康大穰}")
    private String serverName;
    @Value("${dk.game.server.port}")
    private int port;
    @Value("${dk.game.server.protocol:TCP}")
    private String protocol;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void registerGameServer() {
        GameServerRegisterAdaptor<AbstractSession, AbstractMessage<?>> register;
        switch (GameServerProtocolEnum.valueOf(protocol.toUpperCase())) {
            case TCP:
                register = Registers.newTcpGameServerRegister();
                break;
            case HTTP:
                register = Registers.newHttpGameServerRegister();
                break;
            case WEBSOCKET:
                register = Registers.newWebsocketGameServerRegister();
                break;
            case UDP:
                register = Registers.newUdpGameServerRegister();
                break;
            default:
                throw new BusinessException("unsupported.server.protocol", "不支持的游戏服通信协议:" + protocol);
        }
        register.setGameId(gameId).setGameName(gameName).setServerId(serverId).setServerName(serverName).setPort(port)
                .setTcpNoDelay()
                // 消息编解码器，默认使用JSON格式通信
                .setMessageEncode(new JsonNettyMessageEncoder()).setMessageDecode(new JsonNettyMessageDecoder())
                // 连接建立成功时的逻辑，创建并关联GameSubject对象
                .onConnected(session -> {
                    LogUtil.session("new connection:" + session.getId());
                    GameSubject subject = new InnerGameSubject();
                    session.setSubject(subject);
                    subject.setSession(session);
                })
                // 连接异常时的逻辑，打印日志
                .onConnectException((session, cause) -> LogUtil.error("connect exception"))
                // 消息发送逻辑
                .onMessageSend(AbstractSession::sendMessage)
                // 消息到达处理逻辑，加入本地任务队列
                .onMessageDispatcher(
                        (session, message) -> ReceivedMessageQueue.addMessage(session, (ReqMessage) message));
        DangKangGameStarter starter = DangKangGameStarter.get().gameServer(register);

        if (CollectionUtil.isNotEmpty(packagePrefixes)) {
            starter.packagePrefixes(packagePrefixes);
        }
        if (CollectionUtil.isNotEmpty(springConfigs)) {
            starter.springConfigs(springConfigs);
        }
        if (messageRegister != null) {
            IMessageRegister mRegister = ReflectUtil.newInstance(messageRegister);
            if (mRegister != null) {
                starter.message(mRegister);
            } else {
                throw new BusinessException("message.register.error", "用户定义的消息类注册器实例化失败，class:" + messageRegister);
            }
        }
        if (CollectionUtil.isNotEmpty(messagePackages)) {
            starter.message(Registers.newDangKangMessageRegister(messagePackages));
        }
        if (messageHandlerRegister != null) {
            IMessageHandlerRegister handlerRegister = ReflectUtil.newInstance(messageHandlerRegister);
            if (handlerRegister != null) {
                starter.messageHandler(handlerRegister);
            } else {
                throw new BusinessException("handler.register.error",
                        "用户定义的消息处理类注册器实例化失败，class:" + messageHandlerRegister);
            }
        }
        if (CollectionUtil.isNotEmpty(messageHandlerPackages)) {
            starter.messageHandler(Registers.newDangKangHandlerRegister(messageHandlerPackages));
        }
    }

    private class InnerGameSubject extends GameSubject {
        @Override
        public int getGameId() {
            return gameId;
        }

        @Override
        public int getServerId() {
            return serverId;
        }
    }

    private static class ReceivedMessageQueue {
        private static final IOrderedExecutor<Long> MESSAGE_EXECUTOR = new OrderedQueuePoolExecutor(
                "game-received-message", 3, Integer.MAX_VALUE);

        static void addMessage(AbstractSession session, ReqMessage<Object> message) {
            MESSAGE_EXECUTOR.addTask(session.getId(), new MessageHandleWorker(session, message));
        }

        /**
         * 消息处理任务线程
         */
        private static class MessageHandleWorker extends AbstractOrderedWorker {

            private AbstractSession session;

            private ReqMessage<Object> message;

            MessageHandleWorker(AbstractSession session, ReqMessage<Object> message) {
                this.session = session;
                this.message = message;
            }

            @Override
            protected void action() {
                try {
                    doMessageHandle();
                } catch (MessageException e) {
                    LogUtil.error(e, "执行消息处理逻辑中断, message:" + message);
                    RespMessage resp = new RespMessage();
                    resp.setId(message.getId() + 1);
                    resp.setCode(e.getGameCode().getCode());
                    resp.setParams(e.getMessage());
                    session.sendMessage(resp);
                } catch (Throwable t) {
                    LogUtil.error(t, "执行消息处理逻辑出错, message:" + message);
                    RespMessage resp = new RespMessage();
                    resp.setId(message.getId() + 1);
                    resp.setCode(GameCodeEnum.UNKNOWN_ERROR.getCode());
                    resp.setParams("未知错误");
                    session.sendMessage(resp);
                }
            }

            private void doMessageHandle() {
                if (session.getSubject() == null && !MessageHandlerHolder.allowGuestRequest(message.getId())) {
                    throw new MessageException(GameCodeEnum.MESSAGE_ILLEGAL, "player.not.login", "用户未登录，不能执行该操作");
                }

                MessageHandlerHolder.doMessageHandler(message, session.getSubject());
            }
        }
    }

}
