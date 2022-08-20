package com.wu.config;

import com.wu.init.InitTcc;
import com.wu.interceptor.FeignInterceptor;
import com.wu.netty.NettyClient;
import com.wu.properties.ClientNettyProperties;
import com.wu.strategy.ClientLocalExecuteTryStrategy;
import com.wu.strategy.ClientNettyMsgHandleStrategyContext;
import com.wu.strategy.NotifyWaitSendMsgThreadStrategy;
import com.wu.untils.IPUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author wuzhouwei
 * @date 2022/7/29
 */
@Configuration
@EnableConfigurationProperties
public class Configure {

    @Bean
    public ClientNettyProperties clientNettyProperties(){
        return new ClientNettyProperties();
    }

    @Bean
    public ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext(){
        ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext = new ClientNettyMsgHandleStrategyContext();
        clientNettyMsgHandleStrategyContext.shuffleIp();
        return clientNettyMsgHandleStrategyContext;
    }

    @Bean
    public SpringContextHolder springContextHolder(){
        return new SpringContextHolder();
    }

    @Bean
    public InitTcc initTcc(ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext){
        return new InitTcc(clientNettyMsgHandleStrategyContext);
    }

    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }

    @Bean
    public ClientLocalExecuteTryStrategy clientLocalExecuteTryStrategy(){
        return new ClientLocalExecuteTryStrategy();
    }

    @Bean
    public NotifyWaitSendMsgThreadStrategy notifyWaitSendMsgThreadStrategy(){
        return new NotifyWaitSendMsgThreadStrategy();
    }

    @Bean
    public NettyClient nettyClient(ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext) throws InterruptedException {
        NettyClient nettyClient = new NettyClient(clientNettyMsgHandleStrategyContext);
        nettyClient.start(clientNettyMsgHandleStrategyContext().getIp(),clientNettyMsgHandleStrategyContext().getPort());
        while (nettyClient.getChannel() == null){
            Thread.sleep(500);
        }
        return nettyClient;
    }
}
