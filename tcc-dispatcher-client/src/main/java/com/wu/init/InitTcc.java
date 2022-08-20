package com.wu.init;

import com.wu.annotation.TccClass;
import com.wu.annotation.TccFeign;
import com.wu.annotation.TccMethod;
import com.wu.context.TccContext;
import com.wu.context.TccContextLocal;
import com.wu.entity.TccExecuteEntity;
import com.wu.entity.TccMethodEntity;
import com.wu.enums.TccRoleEnum;
import com.wu.proxy.RpcClientProxy;
import com.wu.strategy.ClientNettyMsgHandleStrategyContext;
import com.wu.untils.WuAopUtils;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
public class InitTcc implements BeanPostProcessor {

    private static final Map<String,Object> SERVICE_PROXY_INSTANCE_MAP = new ConcurrentHashMap<>();

    private static final Map<String,Object> SERVICE_INSTANCE_MAP = new ConcurrentHashMap<>();

    private ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext;

    public InitTcc(ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext) {
        this.clientNettyMsgHandleStrategyContext = clientNettyMsgHandleStrategyContext;
    }

    public InitTcc() {
    }

    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object serviceBean, String beanName) throws BeansException {
        Object temp = WuAopUtils.getTarget(serviceBean);
        TccClass tccClassAnnotation = null;
        Method[] methods = null;
        Object proxy = serviceBean;
        if(temp != null){
            Class<?> clazz = temp.getClass();
            tccClassAnnotation = clazz.getAnnotation(TccClass.class);
            methods = clazz.getMethods();
            proxy = temp;
            serviceBean = proxy;
        }
        Class<?> clazz = serviceBean.getClass();

        if(tccClassAnnotation != null){
            for (Method method : methods) {

                boolean isTccMethod = method.isAnnotationPresent(TccMethod.class);
                if(isTccMethod){
                    TccMethod tccMethodAnnotation = method.getAnnotation(TccMethod.class);
                    String confirmMethod = tccMethodAnnotation.confirmMethod();
                    String cancelMethod = tccMethodAnnotation.cancelMethod();

                    TccMethodEntity tccTargetMethodEntity = new TccMethodEntity(method,serviceBean);
                    TccMethodEntity tccConfirmMethodEntity = new TccMethodEntity(clazz.getMethod(confirmMethod,method.getParameterTypes()),serviceBean);
                    TccMethodEntity tccCancelMethodEntity = new TccMethodEntity(clazz.getMethod(cancelMethod,method.getParameterTypes()),serviceBean);

                    SERVICE_INSTANCE_MAP.put(clazz.getName()+":"+method.getName(),tccTargetMethodEntity);
                    SERVICE_INSTANCE_MAP.put(clazz.getName()+":"+confirmMethod,tccConfirmMethodEntity);
                    SERVICE_INSTANCE_MAP.put(clazz.getName()+":"+cancelMethod,tccCancelMethodEntity);

                    proxy = RpcClientProxy.createCglibProxy(proxy,method,confirmMethod,cancelMethod,clientNettyMsgHandleStrategyContext);

                    TccMethodEntity tccTargetMethodProxyEntity = new TccMethodEntity(method,proxy);
                    TccMethodEntity tccConfirmMethodProxyEntity = new TccMethodEntity(clazz.getMethod(confirmMethod,method.getParameterTypes()),proxy);
                    TccMethodEntity tccCancelMethodProxyEntity = new TccMethodEntity(clazz.getMethod(cancelMethod,method.getParameterTypes()),proxy);

                    SERVICE_PROXY_INSTANCE_MAP.put(clazz.getName()+":"+method.getName(),tccTargetMethodProxyEntity);
                    SERVICE_PROXY_INSTANCE_MAP.put(clazz.getName()+":"+confirmMethod,tccConfirmMethodProxyEntity);
                    SERVICE_PROXY_INSTANCE_MAP.put(clazz.getName()+":"+cancelMethod,tccCancelMethodProxyEntity);
                }
            }
        }

        final Method[] feignMethods = ReflectionUtils.getAllDeclaredMethods(clazz);
        for (Method feignMethod : feignMethods) {
            TccFeign tccFeignAnnotation = AnnotationUtils.findAnnotation(feignMethod, TccFeign.class);
            if(tccFeignAnnotation != null){
                proxy = RpcClientProxy.createJdkProxy(serviceBean,feignMethod);
            }
        }
        return proxy;
    }

//    @SneakyThrows
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//
//        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(TccClass.class);
//        if (serviceMap != null && serviceMap.size() > 0) {
//            Set<Map.Entry<String, Object>> entries = serviceMap.entrySet();
//            for (Map.Entry<String, Object> entry : entries) {
//                Object serviceBean = entry.getValue();
//
//                // 默认类名作为名称
//                Class<?> clazz = serviceBean.getClass();
//                for (Method method : clazz.getMethods()) {
//                    boolean isTccMethod = method.isAnnotationPresent(TccMethod.class);
//                    if(isTccMethod){
//                        TccMethod tccMethodAnnotation = method.getAnnotation(TccMethod.class);
//                        String confirmMethod = tccMethodAnnotation.confirmMethod();
//                        String cancelMethod = tccMethodAnnotation.cancelMethod();
//
//                        TccMethodEntity tccTargetMethodEntity = new TccMethodEntity(method,serviceBean);
//                        TccMethodEntity tccConfirmMethodEntity = new TccMethodEntity(clazz.getMethod(confirmMethod,null),serviceBean);
//                        TccMethodEntity tccCancelMethodEntity = new TccMethodEntity(clazz.getMethod(cancelMethod,null),serviceBean);
//
//                        SERVICE_INSTANCE_MAP.put(clazz.getName()+":"+method.getName(),tccTargetMethodEntity);
//                        SERVICE_INSTANCE_MAP.put(clazz.getName()+":"+confirmMethod,tccConfirmMethodEntity);
//                        SERVICE_INSTANCE_MAP.put(clazz.getName()+":"+cancelMethod,tccCancelMethodEntity);
//                    }
//                }
//            }
//        }
//    }

    public static TccMethodEntity getTccMethodEntity(TccExecuteEntity tccExecuteEntity,TccContext tccContext){
        TccContextLocal.getInstance().set(tccContext);
        return (TccMethodEntity) SERVICE_INSTANCE_MAP.get(tccExecuteEntity.getTargetClass()+":"+tccExecuteEntity.getTargetMethod());
    }

    public static TccMethodEntity getTccMethodProxyEntity(TccExecuteEntity tccExecuteEntity){
        TccContextLocal.getInstance().setTccContext(tccExecuteEntity.getTccId(),tccExecuteEntity.getParamArgs(),TccRoleEnum.RPC);
        return (TccMethodEntity) SERVICE_PROXY_INSTANCE_MAP.get(tccExecuteEntity.getTargetClass()+":"+tccExecuteEntity.getTargetMethod());
    }
}
