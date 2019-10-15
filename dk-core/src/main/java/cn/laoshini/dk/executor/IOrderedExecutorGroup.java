package cn.laoshini.dk.executor;

/**
 * @author fagarine
 */
public interface IOrderedExecutorGroup {

    IOrderedExecutor getExecutor(int index);
}
