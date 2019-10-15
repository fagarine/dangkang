package cn.laoshini.dk.executor;

import cn.laoshini.dk.function.VariousWaysManager;

/**
 * @author fagarine
 */
public class DkExecutors {
    private DkExecutors() {
    }

    public static <K> IOrderedExecutor<K> newOrderedExecutor(String name, int corePoolSize, int maxQueueSize) {
        Object[] args = new Object[] { name, corePoolSize, maxQueueSize };
        Class<?>[] types = new Class[] { String.class, int.class, int.class };
        return VariousWaysManager.getFunctionCurrentImpl(IOrderedExecutor.class, args, types);
    }
}
