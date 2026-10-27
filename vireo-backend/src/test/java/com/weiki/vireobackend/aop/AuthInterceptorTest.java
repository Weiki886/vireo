package com.weiki.vireobackend.aop;

import com.weiki.vireobackend.annotation.AuthCheck;
import com.weiki.vireobackend.exception.BusinessException;
import com.weiki.vireobackend.exception.ErrorCode;
import com.weiki.vireobackend.model.entity.User;
import com.weiki.vireobackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthInterceptorTest {

    private AuthInterceptor authInterceptor;

    private UserService userService;

    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setUp() throws Throwable {
        authInterceptor = new AuthInterceptor();
        userService = Mockito.mock(UserService.class);
        ReflectionTestUtils.setField(authInterceptor, "userService", userService);
        joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn("OK");
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void notLoggedInShouldPropagateNotLoginError() throws Throwable {
        when(userService.getLoginUser(Mockito.any()))
                .thenThrow(new BusinessException(ErrorCode.NOT_LOGIN_ERROR));

        assertThatThrownBy(() -> authInterceptor.doInterceptor(joinPoint, authCheck("admin")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.NOT_LOGIN_ERROR.getCode());
        verify(joinPoint, never()).proceed();
    }

    @Test
    void normalUserAccessingAdminMethodShouldBeRejected() throws Throwable {
        when(userService.getLoginUser(Mockito.any())).thenReturn(userWithRole("user"));

        assertThatThrownBy(() -> authInterceptor.doInterceptor(joinPoint, authCheck("admin")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.NO_AUTH_ERROR.getCode());
        verify(joinPoint, never()).proceed();
    }

    @Test
    void unrecognizedUserRoleShouldBeRejected() throws Throwable {
        when(userService.getLoginUser(Mockito.any())).thenReturn(userWithRole("hacker"));

        assertThatThrownBy(() -> authInterceptor.doInterceptor(joinPoint, authCheck("admin")))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.NO_AUTH_ERROR.getCode());
        verify(joinPoint, never()).proceed();
    }

    @Test
    void adminAccessingAdminMethodShouldProceed() throws Throwable {
        when(userService.getLoginUser(Mockito.any())).thenReturn(userWithRole("admin"));

        Object result = authInterceptor.doInterceptor(joinPoint, authCheck("admin"));

        assertThat(result).isEqualTo("OK");
        verify(joinPoint).proceed();
    }

    @Test
    void blankRequiredRoleShouldOnlyRequireLogin() throws Throwable {
        when(userService.getLoginUser(Mockito.any())).thenReturn(userWithRole("user"));

        Object result = authInterceptor.doInterceptor(joinPoint, authCheck(""));

        assertThat(result).isEqualTo("OK");
        verify(joinPoint).proceed();
    }

    private static User userWithRole(String role) {
        User user = new User();
        user.setId(1L);
        user.setUserRole(role);
        return user;
    }

    private static AuthCheck authCheck(String mustRole) {
        return new AuthCheck() {
            @Override
            public String mustRole() {
                return mustRole;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return AuthCheck.class;
            }
        };
    }
}
