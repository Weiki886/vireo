package com.weiki.vireobackend.common;

import lombok.Data;

import java.io.Serializable;

import com.weiki.vireobackend.exception.ErrorCode;

/**
 * 统一响应结构，所有接口返回的 JSON 都包装成该格式。
 *
 * @param <T> 业务数据类型
 */
@Data
public class BaseResponse<T> implements Serializable {

    /**
     * 状态码，0 表示成功，其余见 {@link ErrorCode}
     */
    private int code;

    /**
     * 业务数据
     */
    private T data;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 全参构造响应。
     *
     * @param code    状态码
     * @param data    业务数据
     * @param message 提示信息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 构造不带提示信息的响应。
     *
     * @param code 状态码
     * @param data 业务数据
     */
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 根据错误码构造错误响应。
     *
     * @param errorCode 错误码枚举
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

    private static final long serialVersionUID = 1L;
}
