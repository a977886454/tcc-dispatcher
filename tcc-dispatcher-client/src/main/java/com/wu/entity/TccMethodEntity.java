package com.wu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TccMethodEntity {
    private Method method;
    private Object bean;
}
