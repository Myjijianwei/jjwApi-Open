package com.jjw.project.aop;


import com.jjw.project.exception.BusinessException;
//import com.jjw.project.service.UserInterfaceInfoService;
import com.jjw.project.service.UserInterfaceInfoService;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class invokeCountAOP {

    @Autowired
    private UserInterfaceInfoService userInterfaceInfoService;

    // 定义切入点，这里假设所有的接口方法都在 com.jjw.project.controller 包下
    @Pointcut("execution(* com.jjw.project.controller.*.*(..))")
    public void controllerMethods() {}

    // 定义后置通知，在切入点方法执行之后执行
    @After("controllerMethods()")
    public void afterControllerMethod(org.aspectj.lang.JoinPoint joinPoint) {
        // 获取当前登录用户的 ID 和接口 ID
        Long userId = getUserIdFromRequest(joinPoint);
        Long interfaceId = getInterfaceIdFromRequest(joinPoint);
        if (userId != null && interfaceId != null) {
            incrementInvokeCount(interfaceId, userId);
        }
    }

    private void incrementInvokeCount(long interfaceInfoId, long userId) {
        try {
            boolean result = userInterfaceInfoService.invokeCount(interfaceInfoId, userId);
            if (!result) {
                // 处理更新失败的情况
                System.err.println("调用次数更新失败，接口 ID: " + interfaceInfoId + ", 用户 ID: " + userId);
            }
        } catch (BusinessException e) {
            // 处理参数错误异常
            System.err.println("调用次数更新时参数错误，接口 ID: " + interfaceInfoId + ", 用户 ID: " + userId + ", 错误信息: " + e.getMessage());
        } catch (Exception e) {
            // 处理其他异常
            System.err.println("调用次数更新时发生未知错误，接口 ID: " + interfaceInfoId + ", 用户 ID: " + userId + ", 错误信息: " + e.getMessage());
        }
    }

    private Long getUserIdFromRequest(org.aspectj.lang.JoinPoint joinPoint) {
        // 这里需要根据实际情况从请求中获取用户 ID
        // 示例代码只是简单示意，你需要根据你的项目实际情况修改
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        return null;
    }

    private Long getInterfaceIdFromRequest(org.aspectj.lang.JoinPoint joinPoint) {
        // 这里需要根据实际情况从请求中获取接口 ID
        // 示例代码只是简单示意，你需要根据你的项目实际情况修改
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        return null;
    }
}