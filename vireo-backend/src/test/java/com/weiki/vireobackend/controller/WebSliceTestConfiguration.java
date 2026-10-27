package com.weiki.vireobackend.controller;

import com.weiki.vireobackend.service.UserService;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Web 切片测试（@WebMvcTest）共享的最小根配置。
 * <p>
 * 不能直接使用主启动类作为根配置：它上面的 {@code @MapperScan} 会在没有数据源的
 * 切片上下文中实例化 Mapper。这里只扫描 controller 包，让 @WebMvcTest 指定的
 * Controller 能被注册；Service 边界统一用 Mockito mock 代替（需要自定义桩行为的
 * 测试可在自己的类上用 @MockitoBean 覆盖），MVC 相关自动配置仍由 @WebMvcTest 导入。
 * 放在 controller 包下，避免与 config 包中的全量 @SpringBootTest 互相串上下文。
 */
@SpringBootConfiguration
@ComponentScan(basePackages = "com.weiki.vireobackend.controller")
class WebSliceTestConfiguration {

    @Bean
    UserService userService() {
        return Mockito.mock(UserService.class);
    }
}
