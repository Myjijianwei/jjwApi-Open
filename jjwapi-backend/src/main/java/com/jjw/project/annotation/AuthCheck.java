package com.jjw.project.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解，用于指定方法执行所需的角色权限
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 有任何一个角色即可访问方法
     * 默认值为空数组，表示不做任意角色限制
     *
     * @return 允许的角色列表
     */
    String[] anyRole() default {};

    /**
     * 必须有某个角色才能访问方法
     * 默认值为空字符串，表示不做必须角色限制
     *
     * @return 必须具备的角色
     */
    String mustRole() default "";

    /**
     * 是否需要同时满足 anyRole 和 mustRole 的条件
     * 默认值为 false，表示只需要满足其中一个条件即可
     *
     * @return 是否需要同时满足条件
     */
    boolean requireBoth() default false;
}
