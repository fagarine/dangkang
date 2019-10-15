package cn.laoshini.dk.domain;

/**
 * 消息处理Handler描述信息，对应注解@{@link cn.laoshini.dk.annotation.MessageHandle MessageHandle}
 *
 * @author fagarine
 */
public class HandlerDesc {

    private int id;

    private String description;

    /**
     * 是否允许玩家没有登录的情况下，发送该消息，默认为false
     * 正常情况下，只会有有限的几条消息不用登录就发送，比如用户登录
     */
    private boolean allowGuestRequest;

    /**
     * 消息是否需要按到达顺序执行，默认为true，即需要按顺序执行
     * 实现顺序执行的基本方法是把消息存入队列，然后从队列消费
     * 正常情况下，只会有有限的几条消息不用按顺序执行，比如用户登录
     */
    private boolean sequential = true;

    /**
     * 是否是按当康系统消息Handler规范实现的Handler
     */
    private boolean internal;

    /**
     * 记录参数泛型信息，如果是按当康系统规范的Handler，则进入消息都是泛型，需要记录消息类型
     */
    private Class<?> genericType;

    public HandlerDesc(int id) {
        this.id = id;
    }

    public HandlerDesc(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public HandlerDesc(int id, boolean allowGuestRequest, boolean sequential) {
        this.id = id;
        this.allowGuestRequest = allowGuestRequest;
        this.sequential = sequential;
    }

    public HandlerDesc(int id, String description, boolean allowGuestRequest, boolean sequential) {
        this.id = id;
        this.description = description;
        this.allowGuestRequest = allowGuestRequest;
        this.sequential = sequential;
    }

    public HandlerDesc(int id, String description, boolean allowGuestRequest, boolean sequential, boolean internal,
            Class<?> genericType) {
        this.id = id;
        this.description = description;
        this.allowGuestRequest = allowGuestRequest;
        this.sequential = sequential;
        this.internal = internal;
        this.genericType = genericType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public Class<?> getGenericType() {
        return genericType;
    }

    public void setGenericType(Class<?> genericType) {
        this.genericType = genericType;
    }

    @Override
    public String toString() {
        return "HandlerDesc{" + "id=" + id + ", description='" + description + '\'' + ", allowGuestRequest="
                + allowGuestRequest + ", sequential=" + sequential + ", internal=" + internal + ", genericType="
                + genericType + '}';
    }
}
