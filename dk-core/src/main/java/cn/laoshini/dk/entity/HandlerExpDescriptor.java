package cn.laoshini.dk.entity;

import java.io.Serializable;

import lombok.Data;

import cn.laoshini.dk.constant.InnerTableNameConst;
import cn.laoshini.dk.dao.TableKey;
import cn.laoshini.dk.dao.TableMapping;

/**
 * 表达消息处理Handler逻辑的表达式信息
 *
 * @author fagarine
 */
@Data
@TableMapping(value = InnerTableNameConst.HANDLER_EXPRESSION, description = "消息处理Handler逻辑表达式")
public class HandlerExpDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableKey
    private Integer id;

    /**
     * handler对应的消息id
     */
    private Integer messageId;

    /**
     * 通信协议类型，如果是长连接，则响应消息直接推送给客户端，否则需要在方法体中返回消息
     */
    private String protocol;

    /**
     * 消息体类的全限定名
     */
    private String dataType;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 是否允许玩家没有登录的情况下，发送该消息，默认为false <br>
     * 正常情况下，只会有有限的几条消息不用登录就发送，比如用户登录
     */
    private boolean guest;

    /**
     * 消息是否需要按到达顺序执行，默认为true，即需要按顺序执行<br>
     * 实现顺序执行的基本方法是把消息存入队列，然后从队列消费<br>
     * 正常情况下，只会有有限的几条消息不用按顺序执行，比如用户登录<br>
     * 注意：HTTP连接收到的消息可能会忽略此项，因为HTTP消息默认不会进入队列
     */
    private boolean sequential;

    /**
     * 使用的表达式类型
     */
    private String expressionType;

    /**
     * handler具体逻辑的表达式集合
     */
    private String expressions;

}
