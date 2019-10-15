package cn.laoshini.dk.module;

import cn.laoshini.dk.server.AbstractServer;

/**
 * 暂停所有系统业务功能
 *
 * @author fagarine
 */
public class BusinessPause {
    private BusinessPause() {
    }

    public static void pauseAll() {
        // 暂停所有服务器业务线程
        AbstractServer.pauseAll();
    }

    public static void releaseAll() {
        // 取消所有服务器业务线程的暂停状态
        AbstractServer.unPauseAll();
    }
}
