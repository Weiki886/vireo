package com.weiki.vireobackend.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThrowUtilsTest {

    @Test
    void throwIfWithRuntimeExceptionShouldOnlyThrowWhenConditionTrue() {
        BusinessException ex = new BusinessException(ErrorCode.SYSTEM_ERROR, "boom");

        assertThatCode(() -> ThrowUtils.throwIf(false, ex)).doesNotThrowAnyException();
        assertThatThrownBy(() -> ThrowUtils.throwIf(true, ex))
                .isSameAs(ex);
    }

    @Test
    void throwIfWithErrorCodeShouldThrowBusinessExceptionWithSameCode() {
        assertThatCode(() -> ThrowUtils.throwIf(false, ErrorCode.PARAMS_ERROR))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PARAMS_ERROR.getMessage())
                .extracting("code").isEqualTo(ErrorCode.PARAMS_ERROR.getCode());
    }

    @Test
    void throwIfWithErrorCodeAndMessageShouldUseCustomMessage() {
        assertThatCode(() -> ThrowUtils.throwIf(false, ErrorCode.PARAMS_ERROR, "x"))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "账号重复"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("账号重复")
                .extracting("code").isEqualTo(ErrorCode.PARAMS_ERROR.getCode());
    }
}
