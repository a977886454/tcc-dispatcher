package com.wu.annotation;

import java.lang.annotation.*;

/**
 * @author wuzhouwei
 * @date 2022/7/29
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TccFeign {
}
