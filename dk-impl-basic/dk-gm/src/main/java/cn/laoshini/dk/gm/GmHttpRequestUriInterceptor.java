package cn.laoshini.dk.gm;

import cn.laoshini.dk.gm.constant.GmConstants;
import cn.laoshini.dk.net.msg.IMessageInterceptor;
import cn.laoshini.dk.net.session.NettyHttpSession;

/**
 * HTTP协议的GM消息拦截器，强制要求请求的URI为指定的URI，默认URI为{@link GmConstants#GM_URI}
 *
 * @author fagarine
 */
public class GmHttpRequestUriInterceptor<M> implements IMessageInterceptor<NettyHttpSession, M> {

    private String requestUri = GmConstants.GM_URI;

    public GmHttpRequestUriInterceptor() {
    }

    public GmHttpRequestUriInterceptor(String requestUri) {
        this.requestUri = requestUri;
    }

    public static <M> GmHttpRequestUriInterceptor<M> create() {
        return new GmHttpRequestUriInterceptor<>();
    }

    public static <M> GmHttpRequestUriInterceptor<M> of(String fixedUri) {
        return new GmHttpRequestUriInterceptor<>(fixedUri);
    }

    @Override
    public boolean check(NettyHttpSession session, M msg) {
        return requestUri != null && !requestUri.equals(session.getUri());
    }

    public String getRequestUri() {
        return requestUri;
    }
}
