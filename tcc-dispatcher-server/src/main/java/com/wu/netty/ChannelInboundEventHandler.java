package com.wu.netty;


import com.wu.config.SpringContextHolder;
import com.wu.entity.ClientNettyMsg;
import com.wu.enums.NettyMsgTypeEnum;
import com.wu.strategy.ClientNettyMsgHandleStrategyContext;
import com.wu.untils.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @Description:
 * @Author: wuzhouwei
 * @Date: 2022/3/10
 **/
public class ChannelInboundEventHandler extends SimpleChannelInboundHandler<String> {

    private final ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext = SpringContextHolder.getBean(ClientNettyMsgHandleStrategyContext.class);

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelInboundEventHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        LOGGER.info("接收到消息数据为：" + msg);
        ClientNettyMsg clientNettyMsg = JsonUtils.json2obj(msg, ClientNettyMsg.class);

        clientNettyMsgHandleStrategyContext.messageProcessing(NettyMsgTypeEnum
                .getEnumByMsgType(clientNettyMsg.getMsgType()).getBeanName(),ctx,clientNettyMsg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        clientNettyMsgHandleStrategyContext.removeByChannelId(ctx.channel().id().asLongText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("正常关闭通道");
        clientNettyMsgHandleStrategyContext.removeByChannelId(ctx.channel().id().asLongText());
    }
}
