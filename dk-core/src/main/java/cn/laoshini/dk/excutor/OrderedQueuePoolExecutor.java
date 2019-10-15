package cn.laoshini.dk.excutor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.util.LogUtil;

/**
 * 有序任务队列线程池
 *
 * @author fagarine
 */
@FunctionVariousWays(singleton = false, description = "使用JDK自带的线程池实现")
public class OrderedQueuePoolExecutor extends ThreadPoolExecutor implements IOrderedExecutor<Long> {

    /** 线程池最大线程数 */
    public static final int MAX_POOL_SIZE = 100;

    /**
     * 有序线程队列，这里的key不是用户相关的id，而是与线程池中的线程对应的特征码，具体参考{@link #calcFeatureCode(long)}
     */
    private OrderedQueuePool<Long, AbstractOrderedWorker> pool = new OrderedQueuePool<>();

    private String name;

    private int corePoolSize;

    private int maxQueueSize;

    public OrderedQueuePoolExecutor(String name, int corePoolSize, int maxQueueSize) {
        super(poolSize(corePoolSize), poolSize(2 * corePoolSize), 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        this.name = name;
        this.corePoolSize = corePoolSize;
        this.maxQueueSize = maxQueueSize;
    }

    public OrderedQueuePoolExecutor(int corePoolSize) {
        this("queue-pool", corePoolSize, Integer.MAX_VALUE);
    }

    private static int poolSize(int size) {
        if (size <= 0 || size > MAX_POOL_SIZE) {
            return MAX_POOL_SIZE;
        }

        return size;
    }

    /**
     * 增加执行任务
     * 具有同一特征码的key会被分配到同一个线程的任务队列中，然后按照进入队列的先后顺序执行
     *
     * @param key 任务的标识，用来选择任务队列
     * @param task 待执行任务
     * @return 返回是否成功加入队列
     */
    @Override
    public boolean addTask(Long key, AbstractOrderedWorker task) {
        // 计算key对应的特征码，并获取对应的任务队列
        TaskQueue<AbstractOrderedWorker> queue = pool.getTaskQueue(calcFeatureCode(key));
        // 记录任务是否添加成功
        boolean result;
        // 是否立即执行任务（如果当前任务队列为空，则可以要求任务立即执行）
        boolean run = false;
        synchronized (queue) {
            if (maxQueueSize > 0 && queue.size() > maxQueueSize) {
                LogUtil.error("队列" + name + "(" + key + ")已满，抛弃队列指令!");
                queue.clear();
            }

            result = queue.add(task);
            if (result) {
                task.setTaskQueue(queue);

                /*
                 * 如果队列当前没有任务在执行（所有任务已执行完），则可以立即执行当前任务
                 * 注：这个状态的设置在{@link #afterExecute(Runnable, Throwable)}方法中改变，
                 * 即当任务执行完后，发现没有任务可以执行了，会标记该状态
                 */
                if (queue.isProcessingCompleted()) {
                    queue.setProcessingCompleted(false);
                    run = true;
                }
            } else {
                LogUtil.error("ordered queue队列添加任务失败");
            }
        }

        // 立即执行任务
        if (run) {
            execute(queue.poll());
        }
        return result;
    }

    /**
     * 计算key的特征码，这里简单以key对核心线程数的余数为特征码
     *
     * @param key 用户相关key
     * @return 返回key对应的特征码
     */
    private long calcFeatureCode(long key) {
        return (key < 0 ? 0 : key) % corePoolSize;
    }

    /**
     * 任务执行完成后，检查同一队列中是否还有任务，有则执行下一个任务，否则将队列状态置为已完成
     *
     * @param r 任务线程
     * @param t 异常对象
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        AbstractOrderedWorker worker = (AbstractOrderedWorker) r;
        TaskQueue<AbstractOrderedWorker> queue = worker.getTaskQueue();
        if (queue != null) {
            AbstractOrderedWorker afterWork;
            synchronized (queue) {
                afterWork = queue.poll();
                if (afterWork == null) {
                    queue.setProcessingCompleted(true);
                }
            }

            if (afterWork != null) {
                execute(afterWork);
            }
        }
    }

    /**
     * 获取剩余任务数量
     */
    public int getTotalTaskCount() {
        int count = super.getActiveCount();
        for (TaskQueue<AbstractOrderedWorker> taskQueue : pool.getTaskQueues().values()) {
            count += taskQueue.size();
        }
        return count;
    }
}
