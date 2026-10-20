package com.weiki.vireobackend.common;

import lombok.Data;

import java.io.Serializable;

import com.weiki.vireobackend.exception.ErrorCode;

@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

    private static final long serialVersionUID = 1L;
}
