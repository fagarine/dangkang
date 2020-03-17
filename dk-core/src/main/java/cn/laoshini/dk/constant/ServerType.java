package cn.laoshini.dk.constant;

/**
 * 游戏服务器（按功能、作用分）类型枚举
 *
 * @author fagarine
 */
public enum ServerType {

    /**
     * 游戏服
     */
    GAME,

    /**
     * 游戏管理服务器，与游戏服在同一进程，共用数据，一般负责与后台管理服务器通信
     */
    GM,

    /**
     * 后台管理服务器
     */
    CONSOLE,

    /**
     * 帐号管理，用户登录服务器
     */
    ACCOUNT,

    /**
     * 用户充值、支付服务器
     */
    RECHARGE,

    ;

}
