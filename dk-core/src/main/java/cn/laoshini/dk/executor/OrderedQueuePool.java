package cn.laoshini.dk.executor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 有序任务队列池
 *
 * @author fagarine
 */
public class OrderedQueuePool<K, V> {

    private final Map<K, TaskQueue<V>> taskQueueMap = new ConcurrentHashMap<>();

    /**
     * 获得非空任务队列，如果队列不存在，则创建一个返回
     *
     * @param key 队列key
     * @return 该方法不会返回null
     */
    public TaskQueue<V> getTaskQueue(K key) {
        synchronized (taskQueueMap) {
            return taskQueueMap.computeIfAbsent(key, (k) -> new TaskQueue<>());
        }
    }

    /**
     * 获得全部任务队列
     *
     * @return 该方法不会返回null
     */
    public Map<K, TaskQueue<V>> getTaskQueues() {
        return taskQueueMap;
    }

    /**
     * 移除任务队列
     *
     * @param key 队列key
     */
    public void removeTaskQueue(K key) {
        taskQueueMap.remove(key);
    }
}
