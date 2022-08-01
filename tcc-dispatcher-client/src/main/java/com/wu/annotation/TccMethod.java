package com.wu.annotation;

import java.lang.annotation.*;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TccMethod {
    /**
     * Confirm method string.
     *
     * @return the string
     */
    String confirmMethod() default "";

    /**
     * Cancel method string.
     *
     * @return the string
     */
    String cancelMethod() default "";

}
