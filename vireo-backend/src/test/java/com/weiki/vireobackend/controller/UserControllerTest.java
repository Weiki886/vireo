package com.weiki.vireobackend.controller;

import com.weiki.vireobackend.config.JacksonConfig;
import com.weiki.vireobackend.exception.BusinessException;
import com.weiki.vireobackend.exception.GlobalExceptionHandler;
import com.weiki.vireobackend.exception.ErrorCode;
import com.weiki.vireobackend.model.entity.User;
import com.weiki.vireobackend.model.vo.LoginUserVO;
import com.weiki.vireobackend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({JacksonConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {
    private static final long USER_ID = 1893756824288542721L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void registerShouldReturnWrappedIdAsString() throws Exception {
        when(userService.userRegister(anyString(), anyString(), anyString())).thenReturn(USER_ID);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userAccount":"weiki_01","userPassword":"password123","checkPassword":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                // 雪花 ID 超过 JS 安全整数，必须序列化为字符串
                .andExpect(jsonPath("$.data").value(String.valueOf(USER_ID)))
                .andExpect(jsonPath("$.message").value("ok"));

        verify(userService).userRegister("weiki_01", "password123", "password123");
    }

    @Test
    void loginShouldReturnDesensitizedUser() throws Exception {
        LoginUserVO vo = new LoginUserVO();
        vo.setId(USER_ID);
        vo.setUserAccount("weiki_01");
        vo.setUserName("用户");
        when(userService.userLogin(anyString(), anyString(), any())).thenReturn(vo);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userAccount":"weiki_01","userPassword":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(String.valueOf(USER_ID)))
                .andExpect(jsonPath("$.data.userAccount").value("weiki_01"));
    }

    @Test
    void getLoginUserShouldReturnCurrentUser() throws Exception {
        LoginUserVO vo = new LoginUserVO();
        vo.setId(USER_ID);
        vo.setUserAccount("weiki_01");
        when(userService.getLoginUserVO(any())).thenReturn(vo);
        when(userService.getLoginUser(any())).thenReturn(new User());

        mockMvc.perform(get("/user/get/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userAccount").value("weiki_01"));
    }

    @Test
    void logoutShouldReturnTrue() throws Exception {
        when(userService.userLogout(any())).thenReturn(true);

        mockMvc.perform(post("/user/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void nullBodyShouldReturnParamsError() throws Exception {
        // 请求体为 JSON null 时，@RequestBody 参数为 null，Controller 主动抛参数错误
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAMS_ERROR.getCode()));
    }

    @Test
    void businessExceptionShouldBeMappedToErrorResponse() throws Exception {
        when(userService.userRegister(anyString(), anyString(), anyString()))
                .thenThrow(new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复"));

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userAccount":"weiki_01","userPassword":"password123","checkPassword":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAMS_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value("账号重复"));
    }

    @Test
    void unexpectedErrorShouldBeMaskedAsSystemError() throws Exception {
        when(userService.userLogout(any())).thenThrow(new RuntimeException("db down"));

        mockMvc.perform(post("/user/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SYSTEM_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value("系统错误"));
    }
}
