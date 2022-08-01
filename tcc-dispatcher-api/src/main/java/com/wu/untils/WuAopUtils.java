package com.wu.untils;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;

/**
 * @author wuzhouwei
 * @date 2022/7/29
 */
public class WuAopUtils {
    public static Object getTarget(Object proxy) throws Exception{
        //判断是否是代理对象
        if(AopUtils.isAopProxy(proxy)){
            //cglib 代理
            if(AopUtils.isCglibProxy(proxy)){
                //通过暴力反射拿到代理对象的拦截器属性，从拦截器获取目标对象
                Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
                h.setAccessible(true);
                Object dynamicAdvisedInterceptor = h.get(proxy);

                Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
                advised.setAccessible(true);
                Object target = ((AdvisedSupport)advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
                //返回目标对象
                return target;
            }
            //jdk代理
            if(AopUtils.isJdkDynamicProxy(proxy)){
                //通过暴力反射拿到代理对象的拦截器属性，从拦截器获取目标对象
                Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
                h.setAccessible(true);
                AopProxy aopProxy = (AopProxy) h.get(proxy);

                Field advised = aopProxy.getClass().getDeclaredField("advised");
                advised.setAccessible(true);

                Object target = ((AdvisedSupport)advised.get(aopProxy)).getTargetSource().getTarget();

                return target;
            }
        }
        return null;
    }
}
