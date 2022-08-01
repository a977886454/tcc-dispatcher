package com.wu.proxy;

import cn.hutool.core.util.IdUtil;
import com.wu.config.SpringContextHolder;
import com.wu.constant.TccServerConstant;
import com.wu.context.TccContext;
import com.wu.context.TccContextLocal;
import com.wu.entity.ClientNettyMsg;
import com.wu.entity.TccController;
import com.wu.entity.TccParticipator;
import com.wu.enums.NettyMsgTypeEnum;
import com.wu.enums.TccRoleEnum;
import com.wu.init.InitTcc;
import com.wu.strategy.ClientNettyMsgHandleStrategyContext;
import com.wu.untils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 客户端代理类，创建代理对象
 *
 * @name:
 * @author: wuzhouwei
 * @date: 2022-03-13 23:45
 **/
public class RpcClientProxy {

    public static Object createCglibProxy(Object target, Method targetMethod, String confirmMethod, String cancelMethod) {

        Enhancer enhancer = new Enhancer();
        //2、设置父类（目标类）
        enhancer.setSuperclass(target.getClass());
        //3、设置回调函数
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                ClientNettyMsg clientNettyMsg = new ClientNettyMsg();
                String tccId = SpringContextHolder.getRequestContext().getHeader("tccId");
                String startTccId = null;
                if(targetMethod.equals(method)){
                    // 内部feign调用
                    if(StringUtils.isNotBlank(tccId)){

                        LocalDateTime now = LocalDateTime.now();
                        TccParticipator tccParticipator = new TccParticipator();
                        tccParticipator.setApplicationName(SpringContextHolder.getApplicationName());
                        tccParticipator.setCancelMethod(cancelMethod);
                        tccParticipator.setConfirmMethod(confirmMethod);
                        tccParticipator.setTargetMethod(method.getName());
                        tccParticipator.setCreateTime(now);
                        tccParticipator.setTargetClass(target.getClass().getName());
                        tccParticipator.setTccId(tccId);
                        tccParticipator.setLastTime(now.plusSeconds(TccServerConstant.lastTimeSeconds));
                        tccParticipator.setParamJson(JsonUtils.obj2json(args));

                        clientNettyMsg.setMsgType(NettyMsgTypeEnum.TCC_PARTICIPATOR.getMsgType());
                        clientNettyMsg.setData(JsonUtils.obj2json(tccParticipator));
                        clientNettyMsg.setOperationType(0);
                        TccContext tccContext = new TccContext();
                        tccContext.setParamArgs(args);
                        tccContext.setTccId(tccId);
                        tccContext.setTccRoleEnum(TccRoleEnum.FEIGN);
                        TccContextLocal.getInstance().set(tccContext);
                        //发送
                        ClientNettyMsgHandleStrategyContext.sendMessage(JsonUtils.obj2json(clientNettyMsg),tccId);
                    }else {
                        LocalDateTime now = LocalDateTime.now();
                        TccController tccController = new TccController();
                        tccController.setApplicationName(SpringContextHolder.getApplicationName());
                        tccController.setCancelMethod(cancelMethod);
                        tccController.setConfirmMethod(confirmMethod);
                        tccController.setCreateTime(now);
                        tccController.setTargetClass(target.getClass().getName());
                        tccController.setTccId(IdUtil.fastSimpleUUID());
                        tccController.setLastTime(now.plusSeconds(TccServerConstant.lastTimeSeconds));
                        tccController.setTargetMethod(method.getName());
                        tccController.setParamJson(JsonUtils.obj2json(args));

                        clientNettyMsg.setMsgType(NettyMsgTypeEnum.TCC_START.getMsgType());
                        clientNettyMsg.setData(JsonUtils.obj2json(tccController));
                        clientNettyMsg.setOperationType(0);
                        startTccId = tccController.getTccId();
                        TccContext tccContext = new TccContext();
                        tccContext.setParamArgs(args);
                        tccContext.setTccId(tccController.getTccId());
                        tccContext.setTccRoleEnum(TccRoleEnum.START);
                        TccContextLocal.getInstance().set(tccContext);

                        //发送
                        ClientNettyMsgHandleStrategyContext.sendMessage(JsonUtils.obj2json(clientNettyMsg),tccController.getTccId());
                    }

                }
                Object invoke = null;//执行目标
                try {

                    invoke = method.invoke(target, args);

                    if(targetMethod.equals(method)){
                        if(StringUtils.isNotBlank(tccId)){
                            clientNettyMsg.setMsgType(NettyMsgTypeEnum.TCC_PARTICIPATOR.getMsgType());
                            TccParticipator tccParticipator = new TccParticipator();
                            tccParticipator.setStatus(1);
                            tccParticipator.setTccId(TccContextLocal.getInstance().get().getTccId());
                            tccParticipator.setTargetClass(target.getClass().getName());
                            clientNettyMsg.setData(JsonUtils.obj2json(tccParticipator));
                            clientNettyMsg.setOperationType(1);
                            //发送全局提交成功消息
                            ClientNettyMsgHandleStrategyContext.sendMessage(JsonUtils.obj2json(clientNettyMsg),tccParticipator.getTccId());
                        }else {
                            clientNettyMsg.setMsgType(NettyMsgTypeEnum.TCC_SUCCESS.getMsgType());
                            clientNettyMsg.setData(startTccId);
                            clientNettyMsg.setOperationType(3);
                            //发送全局提交成功消息
                            ClientNettyMsgHandleStrategyContext.sendMessage(JsonUtils.obj2json(clientNettyMsg),startTccId);
                        }


                    }
                    return invoke;

                } catch (Exception e) {
                    if(targetMethod.equals(method)) {
                        if(StringUtils.isNotBlank(tccId)){
                            clientNettyMsg.setMsgType(NettyMsgTypeEnum.TCC_PARTICIPATOR.getMsgType());
                            TccParticipator tccParticipator = new TccParticipator();
                            tccParticipator.setStatus(2);
                            tccParticipator.setTccId(TccContextLocal.getInstance().get().getTccId());
                            tccParticipator.setTargetClass(target.getClass().getName());
                            clientNettyMsg.setData(JsonUtils.obj2json(tccParticipator));
                            clientNettyMsg.setOperationType(1);
                            //发送回滚消息
                            ClientNettyMsgHandleStrategyContext.sendMessage(JsonUtils.obj2json(clientNettyMsg),tccParticipator.getTccId());
                        }else {
                            clientNettyMsg.setMsgType(NettyMsgTypeEnum.TCC_ROLLBACK.getMsgType());
                            clientNettyMsg.setData(startTccId);
                            clientNettyMsg.setOperationType(4);
                            //发送全局回滚消息
                            ClientNettyMsgHandleStrategyContext.sendMessage(JsonUtils.obj2json(clientNettyMsg),startTccId);
                        }
                    }
                    throw e;

                }finally {
                    TccContextLocal.getInstance().remove();
                }
            }
        });
        //4、创建代理对象
        return enhancer.create();
    }

    public static Object createJdkProxy(Object target,Method targetMethod) {
        return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(targetMethod.getName().equals(method.getName()) &&
                        Arrays.equals(targetMethod.getParameterTypes(),method.getParameterTypes()) &&
                targetMethod.getReturnType().equals(method.getReturnType())){
                    TccContextLocal.getInstance().get().setIsFeign(true);
                }

                Object invoke = null;
                try {
                    invoke = method.invoke(target,args);

                } catch (Throwable e) {
                    throw e;
                } finally {
                    if(targetMethod.getName().equals(method.getName()) &&
                            Arrays.equals(targetMethod.getParameterTypes(),method.getParameterTypes()) &&
                            targetMethod.getReturnType().equals(method.getReturnType())){
                        TccContextLocal.getInstance().get().setIsFeign(null);
                    }
                }

                return invoke;
            }
        });
    }
}