package com.wu.strategy;

import com.wu.entity.ClientNettyMsg;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
public interface TransactionalHandle {
    void handle(ChannelHandlerContext ctx, ClientNettyMsg clientNettyMsg);
}
