package com.wu.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wu.constant.TccServerConstant;
import com.wu.entity.ClientNettyMsg;
import com.wu.entity.TccController;
import com.wu.entity.TccExecuteEntity;
import com.wu.entity.TccParticipator;
import com.wu.enums.NettyMsgTypeEnum;
import com.wu.mapper.TccControllerMapper;
import com.wu.mapper.TccParticipatorMapper;
import com.wu.untils.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
@Component
public class TransactionalFinalStrategy implements TransactionalHandle {
    @Resource
    private TccParticipatorMapper tccParticipatorMapper;
    @Resource
    private TccControllerMapper tccControllerMapper;
    @Resource
    private ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(ChannelHandlerContext ctx, ClientNettyMsg clientNettyMsg) {
        TccController tccController = tccControllerMapper.selectOne(new LambdaQueryWrapper<TccController>()
                .eq(TccController::getTccId,clientNettyMsg.getData()));

        if(tccController == null){
            clientNettyMsg.setData(ClientNettyMsgHandleStrategyContext.SUCCESS);
            ctx.writeAndFlush(clientNettyMsg);
        }else {
            List<TccParticipator> tccParticipators = tccParticipatorMapper.selectList(new LambdaQueryWrapper<TccParticipator>()
                    .eq(TccParticipator::getTccId, clientNettyMsg.getData()));

            // 使其rpc执行本地方法
            TccExecuteEntity tccExecuteEntity = new TccExecuteEntity();
            tccExecuteEntity.setTccId(tccController.getTccId());
            ClientNettyMsg tempNettyMsg = new ClientNettyMsg();

            tempNettyMsg.setMsgType(NettyMsgTypeEnum.CLIENT_LOCAL_EXECUTE.getMsgType());
            if(clientNettyMsg.getOperationType() == 3){
                for (TccParticipator tccParticipator : tccParticipators) {
                    tccExecuteEntity.setTargetMethod(tccParticipator.getConfirmMethod());
                    tccExecuteEntity.setTargetClass(tccParticipator.getTargetClass());
                    tccExecuteEntity.setParamArgs(JsonUtils.json2obj(tccParticipator.getParamJson(),Object[].class));
                    tempNettyMsg.setData(JsonUtils.obj2json(tccExecuteEntity));
                    ChannelHandlerContext channelHandlerContext = clientNettyMsgHandleStrategyContext.get(tccParticipator.getApplicationName());
                    if(channelHandlerContext != null){
                        int count = tccParticipatorMapper.accumulateRetryCount(LocalDateTime.now().plusSeconds(TccServerConstant.lastTimeSeconds),
                                TccServerConstant.maxRetryCount,tccParticipator.getId(),2);
                        if(count != 0){
                            channelHandlerContext.writeAndFlush(JsonUtils.obj2json(tempNettyMsg));
                        }
                    }
                }

                tccExecuteEntity.setTargetClass(tccController.getTargetClass());
                tccExecuteEntity.setTargetMethod(tccController.getConfirmMethod());
                tccExecuteEntity.setParamArgs(JsonUtils.json2obj(tccController.getParamJson(),Object[].class));
                tempNettyMsg.setData(JsonUtils.obj2json(tccExecuteEntity));
                ChannelHandlerContext channelHandlerContext = clientNettyMsgHandleStrategyContext.get(tccController.getApplicationName());
                if(channelHandlerContext != null){
                    int count = tccControllerMapper.accumulateRetryCount(LocalDateTime.now().plusSeconds(TccServerConstant.lastTimeSeconds),
                            TccServerConstant.maxRetryCount,tccController.getTccId(),1);
                    if(count != 0){
                        channelHandlerContext.writeAndFlush(JsonUtils.obj2json(tempNettyMsg));
                    }
                }

            }else if(clientNettyMsg.getOperationType() == 4){
                for (TccParticipator tccParticipator : tccParticipators) {
                    tccExecuteEntity.setTargetClass(tccParticipator.getTargetClass());
                    tccExecuteEntity.setTargetMethod(tccParticipator.getCancelMethod());
                    tccExecuteEntity.setParamArgs(JsonUtils.json2obj(tccParticipator.getParamJson(),Object[].class));
                    tempNettyMsg.setData(JsonUtils.obj2json(tccExecuteEntity));
                    ChannelHandlerContext channelHandlerContext = clientNettyMsgHandleStrategyContext.get(tccParticipator.getApplicationName());
                    if(channelHandlerContext != null){
                        int count = tccParticipatorMapper.accumulateRetryCount(LocalDateTime.now().plusSeconds(TccServerConstant.lastTimeSeconds),
                                TccServerConstant.maxRetryCount,tccParticipator.getId(),2);
                        if(count != 0){
                            channelHandlerContext.writeAndFlush(JsonUtils.obj2json(tempNettyMsg));
                        }
                    }
                }

                tccExecuteEntity.setTargetClass(tccController.getTargetClass());
                tccExecuteEntity.setTargetMethod(tccController.getCancelMethod());
                tccExecuteEntity.setParamArgs(JsonUtils.json2obj(tccController.getParamJson(),Object[].class));
                tempNettyMsg.setData(JsonUtils.obj2json(tccExecuteEntity));
                ChannelHandlerContext channelHandlerContext = clientNettyMsgHandleStrategyContext.get(tccController.getApplicationName());
                if(channelHandlerContext != null){
                    int count = tccControllerMapper.accumulateRetryCount(LocalDateTime.now().plusSeconds(TccServerConstant.lastTimeSeconds),
                            TccServerConstant.maxRetryCount,tccController.getTccId(),2);
                    if(count != 0){
                        channelHandlerContext.writeAndFlush(JsonUtils.obj2json(tempNettyMsg));
                    }
                }
            }
        }

    }
}
