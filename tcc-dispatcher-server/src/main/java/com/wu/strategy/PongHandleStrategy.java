package com.wu.strategy;

import com.wu.entity.ClientNettyMsg;
import com.wu.untils.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;

/**
 * @author wuzhouwei
 * @date 2022/3/10
 */
@Service
public class PongHandleStrategy implements TransactionalHandle{

    private static final String PONG = "pong";

    @Override
    public void handle(ChannelHandlerContext ctx, ClientNettyMsg clientNettyMsg) {
        clientNettyMsg.setData(PONG);
        ctx.writeAndFlush(JsonUtils.obj2json(clientNettyMsg));
    }
}
