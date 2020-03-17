package cn.laoshini.dk.domain.responese;

import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.exception.DkRuntimeException;

/**
 * @author fagarine
 */
public class Result<T> {

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 说明消息
     */
    private String message;

    /**
     * 业务数据
     */
    private T data;

    public Result() {
        this.message = ResultCode.SUCCESS.getMessage();
        this.code = ResultCode.SUCCESS.getCode();
    }

    public Result(ResultCode resultCode) {
        this.message = resultCode.getMessage();
        this.code = resultCode.getCode();
    }

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(T data, Integer code, String message) {
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS);
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setData(data);
        return r;
    }

    public static <T> Result<T> fail() {
        return new Result<>(ResultCode.FAIL);
    }

    public static <T> Result<T> fail(String message) {
        Result<T> r = new Result<>(ResultCode.FAIL);
        r.setMessage(message);
        return r;
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        Result<T> r = new Result<>(resultCode);
        r.setMessage(resultCode.getMessage());
        return r;
    }

    public static <T> Result<T> fail(String message, Throwable e) {
        if (e instanceof BusinessException) {
            return fail(String.format("%s, exception: %s", message, e.getMessage()));
        }

        Result<T> r = new Result<>(ResultCode.ERROR);
        r.setMessage(message + ", exception:" + e.getMessage());
        return r;
    }

    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode() == this.getCode();
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 提取真实返回结果数据，如果返回错误时，抛出异常
     *
     * @return 返回结果
     */
    public T extractReturn() {
        if (!isSuccess()) {
            if (code == ResultCode.FAIL.getCode()) {
                throw new BusinessException("response.business.exception", message);
            }
            throw new DkRuntimeException("response.unknown.exception", message);
        }
        return data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Result{" + "code=" + code + ", message='" + message + '\'' + ", data=" + data + '}';
    }
}
