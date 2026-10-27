package com.weiki.vireobackend.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.weiki.vireobackend.config.JacksonConfig;
import com.weiki.vireobackend.exception.BusinessException;
import com.weiki.vireobackend.exception.GlobalExceptionHandler;
import com.weiki.vireobackend.exception.ErrorCode;
import com.weiki.vireobackend.model.entity.User;
import com.weiki.vireobackend.model.vo.LoginUserVO;
import com.weiki.vireobackend.model.vo.UserVO;
import com.weiki.vireobackend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = WebSliceTestConfiguration.class)
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
    void adminAddUserShouldReturnNewId() throws Exception {
        loginAsAdmin();
        when(userService.addUser(any())).thenReturn(USER_ID);

        mockMvc.perform(post("/user/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userAccount":"new_user","userName":"新用户","userRole":"user"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(String.valueOf(USER_ID)));

        verify(userService).addUser(argThat(request ->
                "new_user".equals(request.getUserAccount())
                        && "新用户".equals(request.getUserName())
                        && "user".equals(request.getUserRole())));
    }

    @Test
    void adminGetUserVoShouldReturnDesensitizedUser() throws Exception {
        loginAsAdmin();
        User user = new User();
        user.setId(USER_ID);
        user.setUserAccount("weiki_01");
        user.setUserPassword("$2a$10$secret-hash");
        UserVO userVO = new UserVO();
        userVO.setId(USER_ID);
        userVO.setUserAccount("weiki_01");
        when(userService.getById(anyLong())).thenReturn(user);
        when(userService.getUserVO(any())).thenReturn(userVO);

        mockMvc.perform(get("/user/get/vo").param("id", String.valueOf(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(String.valueOf(USER_ID)))
                .andExpect(jsonPath("$.data.userAccount").value("weiki_01"))
                .andExpect(jsonPath("$.data.userPassword").doesNotExist());
    }

    @Test
    void adminDeleteUserShouldReturnTrue() throws Exception {
        loginAsAdmin();
        when(userService.removeById(anyLong())).thenReturn(true);

        mockMvc.perform(post("/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + USER_ID + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        verify(userService).removeById(USER_ID);
    }

    @Test
    void adminUpdateUserShouldReturnTrue() throws Exception {
        loginAsAdmin();
        when(userService.updateUser(any())).thenReturn(true);

        mockMvc.perform(post("/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":%d,"userName":"新昵称","userRole":"admin"}
                                """.formatted(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        verify(userService).updateUser(argThat(request ->
                USER_ID == request.getId() && "新昵称".equals(request.getUserName())));
    }

    @Test
    void adminListUserVoByPageShouldReturnDesensitizedPage() throws Exception {
        loginAsAdmin();
        User user = new User();
        user.setId(USER_ID);
        user.setUserAccount("weiki_01");
        user.setUserPassword("$2a$10$secret-hash");
        UserVO userVO = new UserVO();
        userVO.setId(USER_ID);
        userVO.setUserAccount("weiki_01");
        Page<User> userPage = new Page<>(1, 10, 1);
        userPage.setRecords(List.of(user));
        when(userService.getQueryWrapper(any())).thenReturn(QueryWrapper.create());
        when(userService.page(any(), any(QueryWrapper.class))).thenReturn(userPage);
        when(userService.getUserVO(any())).thenReturn(userVO);

        mockMvc.perform(post("/user/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pageNum":1,"pageSize":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].id").value(String.valueOf(USER_ID)))
                .andExpect(jsonPath("$.data.records[0].userPassword").doesNotExist())
                .andExpect(jsonPath("$.data.totalRow").value(1));
    }

    @Test
    void invalidPageRequestShouldReturnParamsError() throws Exception {
        loginAsAdmin();

        mockMvc.perform(post("/user/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pageNum":0,"pageSize":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAMS_ERROR.getCode()));
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

    private void loginAsAdmin() {
        User adminUser = new User();
        adminUser.setId(USER_ID);
        adminUser.setUserRole("admin");
        when(userService.getLoginUser(any())).thenReturn(adminUser);
    }

}
