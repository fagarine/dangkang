package cn.laoshini.dk.net.session;

/**
 * 会话对象创建者接口定义
 *
 * @author fagarine
 */
@FunctionalInterface
public interface ISessionCreator<S> {

    ISessionCreator<AbstractSession> DK_SESSION_CREATOR = s -> s;

    /**
     * 创建一个业务会话对象并返回
     *
     * @param innerSession 当康系统默认会话对象
     * @return 该方法不应该返回null
     */
    S newSession(AbstractSession innerSession);

}
