package cn.laoshini.dk.domain.msg;

import java.io.Serializable;

import cn.laoshini.dk.constant.GameCodeEnum;

/**
 * 游戏交互消息结构定义抽象类
 *
 * @author fagarine
 */
public abstract class AbstractMessage<Type> implements IMessage<Type>, Serializable {

    /**
     * 消息id
     */
    protected int id;

    /**
     * 返回码，默认为200，否则为错误码
     */
    protected int code = GameCodeEnum.OK.getCode();

    /**
     * 扩展字段，预留
     */
    protected String params = "";

    /**
     * 具体的消息内容
     */
    protected Type data;

    /**
     * 消息内容的类型
     */
    protected Class<Type> dataType;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getParams() {
        return params;
    }

    @Override
    public void setParams(String params) {
        this.params = params;
    }

    @Override
    public Type getData() {
        return data;
    }

    @Override
    public void setData(Type data) {
        this.data = data;
    }

    @Override
    public Class<Type> getDataType() {
        return dataType;
    }

    @Override
    public void setDataType(Class<Type> dataType) {
        this.dataType = dataType;
    }
}
