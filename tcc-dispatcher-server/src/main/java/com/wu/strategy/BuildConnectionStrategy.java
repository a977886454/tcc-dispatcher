package com.wu.strategy;

import com.wu.entity.ClientNettyMsg;
import com.wu.enums.NettyMsgTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
@Component
@Slf4j
public class BuildConnectionStrategy implements TransactionalHandle {

    @Resource
    private ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext;

    @Override
    public void handle(ChannelHandlerContext ctx, ClientNettyMsg clientNettyMsg) {
        clientNettyMsgHandleStrategyContext.put(clientNettyMsg.getData(),ctx);
        clientNettyMsg.setMsgType(NettyMsgTypeEnum.NOTIFY_WAIT_SEND_MSG_THREAD.getMsgType());
        ctx.writeAndFlush(clientNettyMsg);
        log.info(clientNettyMsg.getData()+"应用建立连接");
    }
}
