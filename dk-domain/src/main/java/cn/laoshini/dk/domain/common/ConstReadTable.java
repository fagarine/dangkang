package cn.laoshini.dk.domain.common;

/**
 * 记录从文件中读取出的一个表单的数据
 *
 * @author fagarine
 */
public class ConstReadTable<RowType> extends ConstTable<RowType> {

    /**
     * 读取数据时是否有错误
     */
    private boolean faulty;

    /**
     * 读取错误记录
     */
    private String errorMsg;

    public boolean isFaulty() {
        return faulty;
    }

    public void setFaulty(boolean faulty) {
        this.faulty = faulty;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "ConstReadTable{" + "faulty=" + faulty + ", errorMsg='" + errorMsg + '\'' + "} " + super.toString();
    }
}
