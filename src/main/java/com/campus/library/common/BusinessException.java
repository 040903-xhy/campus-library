package com.campus.library.common;

/**
 * 业务异常：Service 层校验失败时抛出，由全局异常处理器统一转成 JSON 返回。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
