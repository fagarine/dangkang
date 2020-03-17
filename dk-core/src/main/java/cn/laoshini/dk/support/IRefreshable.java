package cn.laoshini.dk.support;

/**
 * 可刷新数据对象接口
 *
 * @author fagarine
 */
@FunctionalInterface
public interface IRefreshable {

    /**
     * 执行刷新数据操作
     */
    void refresh();
}
