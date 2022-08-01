package com.wu.netty;


import com.wu.config.SpringContextHolder;
import com.wu.entity.ClientNettyMsg;
import com.wu.enums.NettyMsgTypeEnum;
import com.wu.strategy.ClientNettyMsgHandleStrategyContext;
import com.wu.untils.IPUtils;
import com.wu.untils.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;


/**
 * @author wuzhouwei
 */
@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<String> {

    private final ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext = SpringContextHolder.getBean(ClientNettyMsgHandleStrategyContext.class);

    private NettyClient client;

    public NettyClientHandler(NettyClient client) {
        this.client = client;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case WRITER_IDLE:

                    ClientNettyMsg chatMsg = new ClientNettyMsg();
                    chatMsg.setMsgType(NettyMsgTypeEnum.PING.getMsgType());
                    chatMsg.setData("ping");
                    ctx.writeAndFlush(JsonUtils.obj2json(chatMsg));
                    log.info("send ping to server----------");
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Thread.sleep(2000);
        super.channelInactive(ctx);
        client.doConnect(clientNettyMsgHandleStrategyContext.getIp(), clientNettyMsgHandleStrategyContext.getPort());
        log.info("重新連接了呀。。。。");
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("接收到的信息：" + msg);
        ClientNettyMsg clientNettyMsg = JsonUtils.json2obj(msg, ClientNettyMsg.class);
        clientNettyMsgHandleStrategyContext.messageProcessing(NettyMsgTypeEnum
                .getEnumByMsgType(clientNettyMsg.getMsgType()).getBeanName(),ctx,clientNettyMsg);
    }
}
