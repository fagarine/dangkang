package cn.laoshini.dk.robot.bt.node;

/**
 * 根节点
 *
 * @author fagarine
 */
public class BtRootNode extends AbstractBtNode {
    /**
     * 根节点顺序执行它的所有直接子节点，执行完成后始终返回true
     */
    @Override
    public boolean tick() {
        if (null != children) {
            for (IBtNode node : children) {
                node.tick();
            }
        }
        return true;
    }
}
