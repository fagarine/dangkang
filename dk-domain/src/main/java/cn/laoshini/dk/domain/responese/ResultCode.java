package cn.laoshini.dk.domain.responese;

/**
 * @author fagarine
 */
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(0, "成功"),

    FAIL(1, "业务失败"),

    ERROR(2, "执行错误");

    private int code;
    private String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
