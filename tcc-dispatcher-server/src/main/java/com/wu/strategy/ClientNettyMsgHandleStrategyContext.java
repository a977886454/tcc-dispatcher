package com.wu.strategy;

import com.wu.entity.ClientNettyMsg;
import com.wu.netty.HeartBeatHandler;
import com.wu.utils.RandomUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author wuzhouwei
 * @date 2022/3/10
 * 客户端的netty消息处理策略上下文
 * 并维护用户所有的通道
 */
@Component
@Slf4j
public class ClientNettyMsgHandleStrategyContext {

    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";

    @Value("${netty.port}")
    private Integer port;

    @Resource
    private Map<String, TransactionalHandle> transactionalHandleMap;

    private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private static final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();


    /**
     * @Description: 登录成功后，这里就有对应登录后的用户记录，这里记录查不到就说明用户未登录
     * @Author: wuzhouwei
     * @Date: 2022/3/13
     * @param null:
     * @return:
     **/
    private static final Map<String, ChannelHandlerContext> userChannelMap = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> clusterCountMap = new ConcurrentHashMap<>();

    public void messageProcessing(String beanName, ChannelHandlerContext ctx, ClientNettyMsg clientNettyMsg){
        transactionalHandleMap.get(beanName).handle(ctx,clientNettyMsg);
    }

    public void put(String applicationName,ChannelHandlerContext ctx){
        try {
            writeLock.lock();
            AtomicInteger atomicInteger = clusterCountMap.get(applicationName);
            if(atomicInteger == null){
                atomicInteger = new AtomicInteger();
                clusterCountMap.put(applicationName,atomicInteger);
            }
            userChannelMap.put(applicationName+":"+ atomicInteger.incrementAndGet(),ctx);
        } finally {
            writeLock.unlock();
        }
    }

    public void putIfAbsent(String userId,ChannelHandlerContext ctx){
        userChannelMap.putIfAbsent(userId,ctx);
    }

    public void removeByUserId(String userId){
        remove(userChannelMap.remove(userId));
    }

    public void removeByChannelId(String channelId){
        try {
            writeLock.lock();
            for (Map.Entry<String, ChannelHandlerContext> channelHandlerContextEntry : userChannelMap.entrySet()) {
                ChannelHandlerContext ctx = channelHandlerContextEntry.getValue();
                if(channelId.equals(ctx.channel().id().asLongText())){
                    String userId = channelHandlerContextEntry.getKey();
                    log.info("客户端用户Id：{},断开连接",userId);
                    remove(userChannelMap.remove(userId));
                    break;
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public ChannelHandlerContext get(String applicationName){
        try {
            readLock.lock();
            AtomicInteger atomicInteger = clusterCountMap.get(applicationName);
            if(atomicInteger == null){
                return null;
            }
            return userChannelMap.get(applicationName+":"+ RandomUtil.getSpecifiedRangeRandom(1, atomicInteger.get()));
        } finally {
            readLock.unlock();
        }
    }

    public Integer getPort(){
        return port;
    }

    public  void remove(ChannelHandlerContext ctx){
        // 关闭channel前,需要先移除Handler, 避免关闭channel后重登造成循环重连问题(selector在轮训的事件的时候发现事件对应channel关闭，就会一直触发轮训)
        ctx.pipeline().remove(ChunkedWriteHandler.class);
        ctx.pipeline().remove(WebSocketServerProtocolHandler.class);
        ctx.pipeline().remove(IdleStateHandler.class);
        ctx.pipeline().remove(HeartBeatHandler.class);
        try {
            ctx.close();
        } catch (Exception e) {
            log.error(String.format("关闭channel异常:%s",e));
        }
    }
}
