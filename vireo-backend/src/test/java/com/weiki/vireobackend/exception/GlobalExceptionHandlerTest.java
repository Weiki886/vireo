package com.weiki.vireobackend.exception;

import com.weiki.vireobackend.common.BaseResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessExceptionShouldKeepCodeAndMessage() {
        BusinessException e = new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");

        BaseResponse<?> response = handler.businessExceptionHandler(e);

        assertThat(response.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode());
        assertThat(response.getMessage()).isEqualTo("账号重复");
        assertThat(response.getData()).isNull();
    }

    @Test
    void unreadableBodyShouldMapToParamsError() {
        BaseResponse<?> response = handler.httpMessageNotReadableHandler(
                new HttpMessageNotReadableException("bad json"));

        assertThat(response.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode());
        assertThat(response.getMessage()).isEqualTo(ErrorCode.PARAMS_ERROR.getMessage());
        assertThat(response.getData()).isNull();
    }

    @Test
    void runtimeExceptionShouldBeMaskedAsSystemError() {
        BaseResponse<?> response = handler.runtimeExceptionHandler(new RuntimeException("db connection refused"));

        // 内部异常细节不能透传给前端
        assertThat(response.getCode()).isEqualTo(ErrorCode.SYSTEM_ERROR.getCode());
        assertThat(response.getMessage()).isEqualTo("系统错误");
        assertThat(response.getData()).isNull();
    }
}
