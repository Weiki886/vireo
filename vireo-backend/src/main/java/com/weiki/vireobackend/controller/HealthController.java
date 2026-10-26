package com.weiki.vireobackend.controller;

import com.weiki.vireobackend.common.BaseResponse;
import com.weiki.vireobackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口，用于探活和确认服务是否正常启动。
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检查。
     *
     * @return 固定返回 "done"
     */
    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("done");
    }
}
