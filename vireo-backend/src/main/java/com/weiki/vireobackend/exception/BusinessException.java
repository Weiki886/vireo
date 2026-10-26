package com.weiki.vireobackend.exception;

import lombok.Getter;

/**
 * 业务异常。Service/Controller 中主动抛出，由全局异常处理器捕获后转成统一响应。
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    /**
     * @param code    自定义错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * @param errorCode 错误码枚举（code 与默认信息都取自它）
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * @param errorCode 错误码枚举（只取它的 code）
     * @param message   自定义错误信息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    private static final long serialVersionUID = 1L;
}
