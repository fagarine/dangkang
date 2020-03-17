package cn.laoshini.dk.register;

import java.util.List;
import java.util.function.Predicate;

import cn.laoshini.dk.domain.dto.GameServerInfoDTO;

/**
 * 游戏GM服务器注册器
 *
 * @param <S> Session的首字母，表示应用内会话类型，对应一个客户端连接
 * @param <M> Message的首字母，表示消息类型
 * @author fagarine
 */
public interface IGmServerRegister<S, M> extends IGameServerRegister<S, M> {

    /**
     * 获取向后台服务器注册游戏服的URL，如果返回不为空，将在GM服务器启动后，向后台服务器发起注册操作{@link #getServerRegisterOperation()}
     *
     * @return 返回后台服务器URL，该方法允许返回null
     */
    String getServerRegisterUrl();

    /**
     * 游戏服向后台管理服务器注册操作逻辑，Predicate的传入的参数为：所有受本GM服务器管理的游戏服的信息，Predicate返回结果为是否注册成功
     *
     * @return 返回服务器注册操作逻辑，该方法允许返回null
     */
    Predicate<List<GameServerInfoDTO>> getServerRegisterOperation();

    /**
     * 后台消息过滤器
     *
     * @return 返回后台消息过滤器，该方法允许返回null
     */
    Predicate<M> getConsoleMessageFilter();
}
