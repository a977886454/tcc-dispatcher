package com.wu.runner;

import com.wu.netty.NettyServer;
import com.wu.schedule.TryTransactionalSchedule;
import com.wu.strategy.ClientNettyMsgHandleStrategyContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
@Component
public class InitRunner implements CommandLineRunner, DisposableBean {

    @Resource
    private NettyServer nettyServer;

    @Resource
    private ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext;

    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    private TryTransactionalSchedule tryTransactionalSchedule;

    @Override
    public void run(String... args) throws Exception {
        if (atomicBoolean.compareAndSet(false,true)) {
            nettyServer.start(new InetSocketAddress(clientNettyMsgHandleStrategyContext.getPort()));
        }
    }

    @Override
    public void destroy() throws Exception {
        tryTransactionalSchedule.shutdown();
    }
}
