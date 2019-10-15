package cn.laoshini.dk.register;

/**
 * 功能注册接口定义
 *
 * @author fagarine
 */
public interface IFunctionRegister {

    /**
     * 返回功能模块名称
     *
     * @return 返回功能模块名称
     */
    String functionName();

    /**
     * 执行功能注册任务
     *
     * @param classLoader 执行任务使用的类加载器
     */
    void action(ClassLoader classLoader);

}
