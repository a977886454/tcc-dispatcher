package com.wu.strategy;

import com.wu.entity.ClientNettyMsg;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author wuzhouwei
 * @date 2022/7/29
 */
public interface ClientExecuteTryStrategy {
    void handle(ChannelHandlerContext ctx, ClientNettyMsg clientNettyMsg);
}
