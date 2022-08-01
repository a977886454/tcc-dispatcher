package com.wu.strategy;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wu.entity.ClientNettyMsg;
import com.wu.entity.TccParticipator;
import com.wu.enums.NettyMsgTypeEnum;
import com.wu.mapper.TccParticipatorMapper;
import com.wu.untils.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
@Component
public class TransactionalParticipatorStrategy implements TransactionalHandle {
    @Resource
    private TccParticipatorMapper tccParticipatorMapper;

    @Override
    public void handle(ChannelHandlerContext ctx, ClientNettyMsg clientNettyMsg) {
        try {
            TccParticipator tccParticipator = JsonUtils.json2obj(clientNettyMsg.getData(), TccParticipator.class);
            if(clientNettyMsg.getOperationType() == 0){
                tccParticipatorMapper.insert(tccParticipator);
            }else {
                tccParticipatorMapper.update(tccParticipator,
                        Wrappers.<TccParticipator>lambdaUpdate()
                                .eq(TccParticipator::getTccId,tccParticipator.getTccId())
                .eq(StringUtils.isNotBlank(tccParticipator.getTargetMethod()),TccParticipator::getTargetMethod,tccParticipator.getTargetMethod())
                .eq(StringUtils.isNotBlank(tccParticipator.getConfirmMethod()),TccParticipator::getConfirmMethod,tccParticipator.getConfirmMethod())
                .eq(StringUtils.isNotBlank(tccParticipator.getCancelMethod()),TccParticipator::getCancelMethod,tccParticipator.getCancelMethod()));
            }
            clientNettyMsg.setMsgType(NettyMsgTypeEnum.NOTIFY_WAIT_SEND_MSG_THREAD.getMsgType());
            ctx.writeAndFlush(JsonUtils.obj2json(clientNettyMsg));
        } catch (Exception e) {
            clientNettyMsg.setData(ClientNettyMsgHandleStrategyContext.FAIL);
            ctx.writeAndFlush(JsonUtils.obj2json(clientNettyMsg));
        }
    }
}
