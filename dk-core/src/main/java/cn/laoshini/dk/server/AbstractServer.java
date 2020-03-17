package cn.laoshini.dk.server;

import java.util.concurrent.atomic.AtomicBoolean;

import cn.laoshini.dk.util.LogUtil;

/**
 * 服务器线程抽象类
 *
 * @author fagarine
 */
public abstract class AbstractServer extends Thread {

    /**
     * 是否关服，关服后不再接受消息处理
     */
    protected AtomicBoolean shutdown = new AtomicBoolean(true);

    /**
     * 是否暂停任务
     */
    protected AtomicBoolean pause = new AtomicBoolean();

    /**
     * 服务启动时间
     */
    private long startTime;

    /**
     * 返回服务器线程名称
     *
     * @return 返回服务器线程名称
     */
    protected abstract String getServerThreadName();

    /**
     * 关闭服务器线程，慎用
     */
    public abstract void shutdown();

    protected void pauseServer() {
        if (!pause.get()) {
            pause.compareAndSet(false, true);
            LogUtil.info("服务器线程 [{}] 暂停执行", getServerThreadName());
        }
    }

    protected void unPauseServer() {
        if (pause.get()) {
            pause.compareAndSet(true, false);
            LogUtil.info("服务器线程 [{}] 解除暂停，重新开始执行", getServerThreadName());
        }
    }

    @Override
    public void run() {
        // 设置线程名称
        setName(getServerThreadName());

        shutdown.set(false);
        startTime = System.currentTimeMillis();
    }

    public boolean isPaused() {
        return pause.get();
    }

    public boolean isShutdown() {
        return shutdown.get();
    }

    public boolean isRunning() {
        return isAlive() && !isPaused() && !isShutdown();
    }

    public long getStartTime() {
        return startTime;
    }
}
