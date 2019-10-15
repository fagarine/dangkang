package cn.laoshini.dk.robot.bt;

/**
 * 行为树配置类接口定义
 *
 * @author fagarine
 */
public interface IBtConfig {

    /**
     * 获取行为树唯一id
     *
     * @return 行为树唯一id
     */
    int getTreeId();

    /**
     * 获取行为树名称
     *
     * @return 行为树名称
     */
    String getTreeName();

    /**
     * 该配置信息是否有效
     *
     * @return 配置信息是否有效
     */
    boolean isValid();
}
