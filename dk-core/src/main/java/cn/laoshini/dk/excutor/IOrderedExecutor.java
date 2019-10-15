package cn.laoshini.dk.excutor;

import cn.laoshini.dk.annotation.ConfigurableFunction;

/**
 * 有序线程池接口
 * <p>
 * 该接口的实现类，需要提供一个包含三个指定参数的构造方法，这三个参数为：
 * <ul>
 * <li>name: 线程池名称</li>
 * <li>corePoolSize: 核心线程数量</li>
 * <li>maxQueueSize: 单个任务队列的最大长度，当队列达到该长度时，新到的任务将被抛弃；仅为正整数时有效</li>
 * </ul>
 * </p>
 *
 * @param <KeyType> 任务标识的类型，该类型需要与实现类使用的{@link OrderedQueuePool}中的 K 类型一致
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.ordered.executor", description = "有序线程池")
public interface IOrderedExecutor<KeyType> {

    /**
     * 添加任务
     *
     * @param key 任务的标识，用来选择任务队列
     * @param task 待执行任务
     * @return 返回是否成功加入队列
     */
    boolean addTask(KeyType key, AbstractOrderedWorker task);
}
