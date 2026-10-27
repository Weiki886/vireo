package com.weiki.vireobackend.common;

import com.weiki.vireobackend.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultUtilsTest {

    @Test
    void successShouldWrapDataWithCodeZeroAndOkMessage() {
        BaseResponse<String> response = ResultUtils.success("done");

        assertThat(response.getCode()).isZero();
        assertThat(response.getData()).isEqualTo("done");
        assertThat(response.getMessage()).isEqualTo("ok");
    }

    @Test
    void successShouldAllowNullData() {
        BaseResponse<Object> response = ResultUtils.success(null);

        assertThat(response.getCode()).isZero();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo("ok");
    }

    @Test
    void errorFromErrorCodeShouldUseItsCodeAndMessage() {
        BaseResponse<?> response = ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR);

        assertThat(response.getCode()).isEqualTo(ErrorCode.NOT_LOGIN_ERROR.getCode());
        assertThat(response.getMessage()).isEqualTo(ErrorCode.NOT_LOGIN_ERROR.getMessage());
        assertThat(response.getData()).isNull();
    }

    @Test
    void errorFromCodeAndMessageShouldKeepBoth() {
        BaseResponse<?> response = ResultUtils.error(40001, "自定义错误");

        assertThat(response.getCode()).isEqualTo(40001);
        assertThat(response.getMessage()).isEqualTo("自定义错误");
        assertThat(response.getData()).isNull();
    }

    @Test
    void errorFromErrorCodeAndMessageShouldOverrideMessage() {
        BaseResponse<?> response = ResultUtils.error(ErrorCode.PARAMS_ERROR, "账号重复");

        assertThat(response.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode());
        assertThat(response.getMessage()).isEqualTo("账号重复");
    }
}
