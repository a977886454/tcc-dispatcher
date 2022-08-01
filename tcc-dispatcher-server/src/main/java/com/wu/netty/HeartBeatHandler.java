package com.wu.netty;

import com.wu.config.SpringContextHolder;
import com.wu.strategy.ClientNettyMsgHandleStrategyContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wuzhouwei
 * @date 2020/4/12
 */
@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private final ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext = SpringContextHolder.getBean(ClientNettyMsgHandleStrategyContext.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;

            if(idleStateEvent.state() == IdleState.READER_IDLE){
                log.info("读空闲事件触发...");
            }else if(idleStateEvent.state() == IdleState.WRITER_IDLE){
                log.info("写空闲事件触发...");
            }else if(idleStateEvent.state() == IdleState.ALL_IDLE){
                // 如果是读写空闲状态(用户断网了), 就关闭该用户的通道
                log.info("读写空闲事件触发,该用户断网,客户端不在发送心跳");
                log.info("关闭该用户通道资源");

                clientNettyMsgHandleStrategyContext.removeByChannelId(ctx.channel().id().asLongText());
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
}
