package cn.laoshini.dk.net.msg;

/**
 * 游戏交互消息定义接口
 *
 * @param <Type> 消息具体内容（除了消息号等基本信息的具体内容）的类型
 * @author fagarine
 */
public interface IMessage<Type> {

    String ID_METHOD = "getId";

    /**
     * 返回消息id
     *
     * @return 返回消息id
     */
    int getId();

    /**
     * 设置消息id
     *
     * @param id 消息id
     */
    void setId(int id);

    /**
     * 获取消息返回码，应答消息使用，请求消息为0
     *
     * @return 消息返回码
     */
    int getCode();

    /**
     * 设置消息返回码，应答消息使用，请求消息为0
     *
     * @param code 消息返回码
     */
    void setCode(int code);

    /**
     * 返回扩展字段，预留
     *
     * @return 可能返回null
     */
    String getParams();

    /**
     * 设置扩展字段信息
     *
     * @param params 扩展信息
     */
    void setParams(String params);

    /**
     * 具体的消息体
     *
     * @return 可能返回null
     */
    Type getData();

    /**
     * 设置具体的消息内容
     *
     * @param data 消息内容
     */
    void setData(Type data);

    /**
     * 返回消息内容data的类型
     *
     * @return 该方法允许返回null，表示消息体为空
     */
    Class<Type> getDataType();

    /**
     * 设置消息内容的类型
     *
     * @param dataType 消息内容的类型
     */
    void setDataType(Class<Type> dataType);
}
