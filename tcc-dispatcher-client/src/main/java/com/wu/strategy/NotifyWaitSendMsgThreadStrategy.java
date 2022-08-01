package com.wu.strategy;

import com.wu.context.TccContextLocal;
import com.wu.entity.ClientNettyMsg;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.locks.LockSupport;

/**
 * @author wuzhouwei
 * @date 2022/7/30
 */
public class NotifyWaitSendMsgThreadStrategy implements ClientExecuteTryStrategy {
    @Override
    public void handle(ChannelHandlerContext ctx, ClientNettyMsg clientNettyMsg) {
        if(!"fail".equals(clientNettyMsg.getData())){
            Thread thread = TccContextLocal.getConcurrentWaitThreadMap().get(clientNettyMsg.getData());
            if(thread != null){
                TccContextLocal.getConcurrentWaitThreadMap().remove(clientNettyMsg.getData());
                LockSupport.unpark(thread);
            }
        }
    }
}
