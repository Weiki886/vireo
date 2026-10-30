package com.weiki.vireobackend.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.weiki.vireobackend.exception.BusinessException;
import com.weiki.vireobackend.exception.ErrorCode;
import com.weiki.vireobackend.model.entity.User;
import com.weiki.vireobackend.model.vo.LoginUserVO;
import com.weiki.vireobackend.model.vo.UserVO;
import com.weiki.vireobackend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerAopTest {

    private static final long USER_ID = 1893756824288542721L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void adminCanAccessAdminUserPage() throws Exception {
        User adminUser = userWithRole("admin");
        when(userService.getLoginUser(any())).thenReturn(adminUser);
        User record = new User();
        record.setId(USER_ID);
        record.setUserAccount("weiki_01");
        record.setUserPassword("$2a$10$secret-hash");
        Page<User> userPage = new Page<>(1, 10, 1);
        userPage.setRecords(List.of(record));
        UserVO userVO = new UserVO();
        userVO.setId(USER_ID);
        userVO.setUserAccount("weiki_01");
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
                .andExpect(jsonPath("$.data.records[0].userPassword").doesNotExist());
    }

    @ParameterizedTest
    @MethodSource("adminEndpoints")
    void normalUserCannotAccessAdminUserEndpoints(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        when(userService.getLoginUser(any())).thenReturn(userWithRole("user"));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NO_AUTH_ERROR.getCode()));

        verify(userService, never()).addUser(any());
        verify(userService, never()).getById(anyLong());
        verify(userService, never()).getUserVO(any());
        verify(userService, never()).removeById(anyLong());
        verify(userService, never()).updateUser(any());
        verify(userService, never()).page(any(), any(QueryWrapper.class));
    }

    @Test
    void guestCannotAccessAdminUserPage() throws Exception {
        when(userService.getLoginUser(any()))
                .thenThrow(new BusinessException(ErrorCode.NOT_LOGIN_ERROR));

        mockMvc.perform(post("/user/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pageNum":1,"pageSize":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_LOGIN_ERROR.getCode()));

        verify(userService, never()).getById(anyLong());
    }

    @Test
    void normalUserCanUpdateOwnProfile() throws Exception {
        User normalUser = userWithRole("user");
        LoginUserVO updatedUser = new LoginUserVO();
        updatedUser.setId(USER_ID);
        updatedUser.setUserName("新昵称");
        updatedUser.setUserRole("user");
        when(userService.getLoginUser(any())).thenReturn(normalUser);
        when(userService.updateCurrentUser(any(), any())).thenReturn(updatedUser);

        mockMvc.perform(post("/user/update/my")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userName":"新昵称"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userName").value("新昵称"))
                .andExpect(jsonPath("$.data.userRole").value("user"));
    }

    @Test
    void guestCannotUpdateOwnProfile() throws Exception {
        when(userService.getLoginUser(any()))
                .thenThrow(new BusinessException(ErrorCode.NOT_LOGIN_ERROR));

        mockMvc.perform(post("/user/update/my")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userName":"新昵称"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_LOGIN_ERROR.getCode()));

        verify(userService, never()).updateCurrentUser(any(), any());
    }

    private static User userWithRole(String role) {
        User user = new User();
        user.setId(USER_ID);
        user.setUserRole(role);
        return user;
    }

    private static Stream<MockHttpServletRequestBuilder> adminEndpoints() {
        return Stream.of(
                post("/user/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userAccount":"new_user","userName":"新用户"}
                                """),
                get("/user/get/vo").param("id", String.valueOf(USER_ID)),
                post("/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":%d}
                                """.formatted(USER_ID)),
                post("/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":%d,"userName":"新昵称"}
                                """.formatted(USER_ID)),
                post("/user/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pageNum":1,"pageSize":10}
                                """)
        );
    }
}
