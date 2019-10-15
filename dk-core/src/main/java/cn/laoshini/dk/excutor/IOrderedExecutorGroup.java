package cn.laoshini.dk.excutor;

/**
 * @author fagarine
 */
public interface IOrderedExecutorGroup {

    IOrderedExecutor getExecutor(int index);
}
