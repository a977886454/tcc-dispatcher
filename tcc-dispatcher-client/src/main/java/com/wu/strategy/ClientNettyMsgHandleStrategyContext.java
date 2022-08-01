package com.wu.strategy;

import com.wu.context.TccContextLocal;
import com.wu.entity.ClientNettyMsg;
import com.wu.untils.FunctionalUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;


/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
public class ClientNettyMsgHandleStrategyContext {
    @Value("${netty.server.port}")
    private Integer port;
    @Value("${netty.server.ips}")
    private String[] ips;

    public static Channel channel;

    public static final AtomicInteger CHOOSE_IP = new AtomicInteger(-1);

    public ClientNettyMsgHandleStrategyContext(){
    }

    @Resource
    private Map<String, ClientExecuteTryStrategy> transactionalHandleMap;

    public Integer getPort() {
        return port;
    }

    public void shuffleIp(){
        Collections.shuffle(FunctionalUtil.asList(ips));

    }

    /**
     * @Description: 这里选择客户端连接server的ip
     * @Author: wuzhouwei
     * @Date: 2022/8/1
     * @return:
     **/
    public String getIp() {
        int index = CHOOSE_IP.incrementAndGet();
        if(ips.length <= index){
            CHOOSE_IP.set(0);
        }
        return ips[CHOOSE_IP.get()];
    }

    public void messageProcessing(String beanName, ChannelHandlerContext ctx, ClientNettyMsg clientNettyMsg){
        transactionalHandleMap.get(beanName).handle(ctx,clientNettyMsg);
    }

    public static void sendBuildConnectionMessage(Object msg,String applicationName) throws InterruptedException {
        Thread currentThread = Thread.currentThread();
        TccContextLocal.getConcurrentWaitThreadMap().put(applicationName,currentThread);
        channel.writeAndFlush(msg);
        long start = System.currentTimeMillis();
        LockSupport.parkNanos(currentThread,5000);
        long end = System.currentTimeMillis();
        if(end-start >= 5000){
            TccContextLocal.getConcurrentWaitThreadMap().remove(applicationName);
            throw new RuntimeException("当前网络阻塞。。。");
        }
    }

    public static void sendMessage(Object msg,String tccId) throws InterruptedException {
        Thread currentThread = Thread.currentThread();
        TccContextLocal.getConcurrentWaitThreadMap().put(tccId,currentThread);
        channel.writeAndFlush(msg);
        long start = System.currentTimeMillis();
        LockSupport.parkNanos(currentThread,5000);
        long end = System.currentTimeMillis();
        if(end-start >= 5000){
            TccContextLocal.getConcurrentWaitThreadMap().remove(tccId);
            throw new RuntimeException("当前网络阻塞。。。");
        }
    }
}
