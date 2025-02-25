package com.jjw.project.aop;

import com.jjw.jjwapicommon.model.entity.User;
import com.jjw.project.annotation.AuthCheck;
import com.jjw.project.common.ErrorCode;
import com.jjw.project.exception.BusinessException;
import com.jjw.project.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

@Aspect
@Component
public class AuthInterceptor {

    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        if (authCheck == null) {
            throw new IllegalArgumentException("AuthCheck annotation is null");
        }
        // 获取请求对象
        HttpServletRequest request = getRequest();
        if (request == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "请求上下文获取失败");
        }

        // 获取当前登录用户
        User user = userService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "未登录或用户信息获取失败");
        }

        // 获取用户角色
        String userRole = user.getUserRole();

        // 拥有任意权限即通过
        List<String> anyRole = Arrays.stream(authCheck.anyRole())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        // 必须有所有权限才通过
        String mustRole = authCheck.mustRole();

        boolean hasAnyRole = CollectionUtils.isEmpty(anyRole) || anyRole.contains(userRole);
        boolean hasMustRole = StringUtils.isBlank(mustRole) || mustRole.equals(userRole);

        if (authCheck.requireBoth()) {
            if (!hasAnyRole || !hasMustRole) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "不具备所需的权限");
            }
        } else {
            if (!hasAnyRole && !hasMustRole) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "不具备所需的权限");
            }
        }

        // 通过权限校验，放行
        return joinPoint.proceed();
    }

    private HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }
}