package cn.laoshini.dk.excutor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 非有序线程池，用于不需要严格按顺序执行的任务
 *
 * @author fagarine
 */
public class NonOrderedQueuePoolExecutor extends ThreadPoolExecutor {
    public NonOrderedQueuePoolExecutor(int corePoolSize) {
        super(corePoolSize, corePoolSize * 2, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }
}
