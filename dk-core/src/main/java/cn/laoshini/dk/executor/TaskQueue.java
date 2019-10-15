package cn.laoshini.dk.executor;

import java.util.ArrayDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 任务队列
 *
 * @author fagarine
 */
public class TaskQueue<TaskType> {
    private Lock lock = new ReentrantLock();
    /**
     * 任务队列
     */
    private final ArrayDeque<TaskType> tasksQueue = new ArrayDeque<>();

    private boolean processingCompleted = true;

    /**
     * 下一执行任务
     *
     * @return
     */
    public TaskType poll() {
        try {
            lock.lock();
            return tasksQueue.poll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 增加任务
     *
     * @param task 任务
     * @return
     */
    public boolean add(TaskType task) {
        try {
            lock.lock();
            return tasksQueue.add(task);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 清理
     */
    public void clear() {
        try {
            lock.lock();
            tasksQueue.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取任务数量
     *
     * @return
     */
    public int size() {
        return tasksQueue.size();
    }

    public boolean isProcessingCompleted() {
        return processingCompleted;
    }

    public void setProcessingCompleted(boolean processingCompleted) {
        this.processingCompleted = processingCompleted;
    }

}
