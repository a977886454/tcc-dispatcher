package com.wu.strategy;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wu.entity.ClientNettyMsg;
import com.wu.entity.TccController;
import com.wu.enums.NettyMsgTypeEnum;
import com.wu.mapper.TccControllerMapper;
import com.wu.untils.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
@Component
public class TransactionalInitiatorStrategy implements TransactionalHandle {

    @Resource
    private TccControllerMapper tccControllerMapper;

    @Override
    public void handle(ChannelHandlerContext ctx, ClientNettyMsg clientNettyMsg) {
        try {
            TccController tccController = JsonUtils.json2obj(clientNettyMsg.getData(), TccController.class);
            if(clientNettyMsg.getOperationType() == 0){
                tccControllerMapper.insert(tccController);
            }else if(clientNettyMsg.getOperationType() == 1){
                tccControllerMapper.update(tccController, Wrappers.<TccController>lambdaUpdate()
                        .eq(TccController::getTccId,tccController.getTccId())
                        .eq(StringUtils.isNotBlank(tccController.getTargetMethod()), TccController::getTargetMethod,tccController.getTargetMethod())
                        .eq(StringUtils.isNotBlank(tccController.getConfirmMethod()), TccController::getConfirmMethod,tccController.getConfirmMethod())
                        .eq(StringUtils.isNotBlank(tccController.getCancelMethod()), TccController::getCancelMethod,tccController.getCancelMethod()));
            }
            clientNettyMsg.setMsgType(NettyMsgTypeEnum.NOTIFY_WAIT_SEND_MSG_THREAD.getMsgType());
            ctx.writeAndFlush(JsonUtils.obj2json(clientNettyMsg));
        } catch (Exception e) {
            clientNettyMsg.setData(ClientNettyMsgHandleStrategyContext.FAIL);
            ctx.writeAndFlush(JsonUtils.obj2json(clientNettyMsg));
        }
    }
}
