package com.weiki.vireobackend.exception;

import com.weiki.vireobackend.common.BaseResponse;
import com.weiki.vireobackend.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。集中捕获 Controller 抛出的异常并转换为统一响应，
 * 避免堆栈信息直接暴露给前端。{@link Hidden} 使其不出现在接口文档中。
 */
@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常，返回异常中携带的错误码与信息。
     *
     * @param e 业务异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 兜底处理未预期的运行时异常，统一返回“系统错误”，避免泄露内部细节。
     *
     * @param e 运行时异常
     * @return 统一错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
