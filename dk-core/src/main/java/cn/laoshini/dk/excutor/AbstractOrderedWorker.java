package cn.laoshini.dk.excutor;

import cn.laoshini.dk.util.LogUtil;

/**
 * 有序队列工作者线程
 *
 * @author fagarine
 */
public abstract class AbstractOrderedWorker implements Runnable {

    @Override
    public void run() {
        try {
            action();
        } catch (Throwable t) {
            LogUtil.error("有序任务执行出错", t);
        }
    }

    /**
     * 执行具体的业务逻辑
     */
    protected abstract void action();

    /**
     * 任务加入队列后，记录所属队列
     */
    private TaskQueue<AbstractOrderedWorker> taskQueue;

    public TaskQueue<AbstractOrderedWorker> getTaskQueue() {
        return taskQueue;
    }

    public void setTaskQueue(TaskQueue<AbstractOrderedWorker> taskQueue) {
        this.taskQueue = taskQueue;
    }
}
