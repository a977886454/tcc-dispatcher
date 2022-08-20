package com.wu.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wu.config.SpringContextHolder;
import com.wu.constant.TccServerConstant;
import com.wu.entity.ClientNettyMsg;
import com.wu.entity.TccController;
import com.wu.entity.TccExecuteEntity;
import com.wu.entity.TccParticipator;
import com.wu.enums.NettyMsgTypeEnum;
import com.wu.mapper.TccControllerMapper;
import com.wu.mapper.TccParticipatorMapper;
import com.wu.strategy.ClientNettyMsgHandleStrategyContext;
import com.wu.untils.FunctionalUtil;
import com.wu.untils.JsonUtils;
import com.wu.utils.EmailUtils;
import com.wu.utils.RandomUtil;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wuzhouwei
 * @date 2022/7/29
 */
public class TryTransactionalSchedule extends AbstractScheduleExecute {
    private final EmailUtils emailUtils = SpringContextHolder.getBean(EmailUtils.class);

    private final TccControllerMapper tccControllerMapper = SpringContextHolder.getBean(TccControllerMapper.class);

    private final TccParticipatorMapper tccParticipatorMapper = SpringContextHolder.getBean(TccParticipatorMapper.class);

    private final ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext = SpringContextHolder.getBean(ClientNettyMsgHandleStrategyContext.class);

    private static final AtomicInteger EMPTY_DATA_COUNT = new AtomicInteger();

    private static final Integer MAX_EMPTY_DATA = 10;

    public TryTransactionalSchedule(String name) {
        super(name);
    }

    @Override
    protected void processTasks() throws InterruptedException {
        List<TccController> tccControllers = tccControllerMapper.selectList(new LambdaQueryWrapper<TccController>()
                .lt(TccController::getRetriedCount, TccServerConstant.maxRetryCount)
                .lt(TccController::getLastTime, LocalDateTime.now()));

        List<TccParticipator> tccParticipators = tccParticipatorMapper.selectList(new LambdaQueryWrapper<TccParticipator>()
                .lt(TccParticipator::getRetriedCount, TccServerConstant.maxRetryCount)
                .lt(TccParticipator::getLastTime, LocalDateTime.now()));

        Map<Integer,Integer> alreadyMap = new HashMap<>();

        Map<String, List<TccParticipator>> tccParticipatorMap = FunctionalUtil.simpleGroupingBy(tccParticipators, TccParticipator::getTccId);
        if(!CollectionUtils.isEmpty(tccControllers)){
            EMPTY_DATA_COUNT.set(0);
            for (TccController tccController : tccControllers) {

                List<TccParticipator> tempTccParticipators = tccParticipatorMap.get(tccController.getTccId());
                if(CollectionUtils.isEmpty(tempTccParticipators)){

                    ChannelHandlerContext channelHandlerContext = clientNettyMsgHandleStrategyContext.get(tccController.getApplicationName());
                    if(channelHandlerContext != null){
                        // TODO tcc事务发起者没执行完，突然宕机了,人工处理或者让客户端提供事务回调查询
                        if(tccController.getStatus() == 0){
                            //不会出现这种情况，只有当除非事务参与的服务只有一个发起者，然后在对应业务提交完事务后突然宕机才会有这情况

                            // 执行confirm
                        }else if(tccController.getStatus() == 1){
                            // 使其rpc执行本地方法
                            TccExecuteEntity tccExecuteEntity = new TccExecuteEntity();
                            tccExecuteEntity.setTccId(tccController.getTccId());
                            tccExecuteEntity.setTargetClass(tccController.getTargetClass());
                            ClientNettyMsg tempNettyMsg = new ClientNettyMsg();

                            tempNettyMsg.setMsgType(NettyMsgTypeEnum.CLIENT_LOCAL_EXECUTE.getMsgType());
                            tccExecuteEntity.setTargetMethod(tccController.getConfirmMethod());
                            tccExecuteEntity.setParamArgs(JsonUtils.json2obj(tccController.getParamJson(),Object[].class));
                            tempNettyMsg.setData(JsonUtils.obj2json(tccExecuteEntity));
                            channelHandlerContext.writeAndFlush(JsonUtils.obj2json(tempNettyMsg));

                            // cancel
                        }else{
                            // 使其rpc执行本地方法
                            TccExecuteEntity tccExecuteEntity = new TccExecuteEntity();
                            tccExecuteEntity.setTccId(tccController.getTccId());
                            tccExecuteEntity.setTargetClass(tccController.getTargetClass());
                            ClientNettyMsg tempNettyMsg = new ClientNettyMsg();

                            tempNettyMsg.setMsgType(NettyMsgTypeEnum.CLIENT_LOCAL_EXECUTE.getMsgType());
                            tccExecuteEntity.setTargetMethod(tccController.getCancelMethod());
                            tccExecuteEntity.setParamArgs(JsonUtils.json2obj(tccController.getParamJson(),Object[].class));
                            tempNettyMsg.setData(JsonUtils.obj2json(tccExecuteEntity));
                            channelHandlerContext.writeAndFlush(JsonUtils.obj2json(tempNettyMsg));
                        }
                    }

                }else {
                    boolean isContinue = false;
                    for (TccParticipator tempTccParticipator : tempTccParticipators) {
                        if(tccController.getStatus() == 0){
                            if(tempTccParticipator.getStatus() == 2){
                                TccController updateTccController = new TccController();
                                updateTccController.setStatus(2);
                                updateTccController.setTccId(tccController.getTccId());
                                tccControllerMapper.updateById(updateTccController);
                                isContinue = true;
                                break;
                            }
                        }
                    }

                    if(isContinue) continue;

                    for (TccParticipator tempTccParticipator : tempTccParticipators) {
                        if(tccController.getStatus() == 0){
                            // TODO 事务发起者服务提交完本地try事务，然后突然宕机就会出现这种server未知事务状态情况，这里可以判断参与者是否失败然后发起回调查看对应该业务是否成功
                            emailUtils.send("tcc事务链路异常，需人工处理","tccId="+tccController.getTccId());
                            break;
                        }

                        ChannelHandlerContext channelHandlerContext = clientNettyMsgHandleStrategyContext.get(tempTccParticipator.getApplicationName());
                        alreadyMap.put(tempTccParticipator.getId(),tempTccParticipator.getId());
                        if(channelHandlerContext != null){
                            if(tccController.getStatus() == 1){
                                TccExecuteEntity tccExecuteEntity = new TccExecuteEntity();
                                tccExecuteEntity.setTccId(tccController.getTccId());
                                tccExecuteEntity.setTargetClass(tempTccParticipator.getTargetClass());
                                ClientNettyMsg tempNettyMsg = new ClientNettyMsg();

                                tempNettyMsg.setMsgType(NettyMsgTypeEnum.CLIENT_LOCAL_EXECUTE.getMsgType());
                                tccExecuteEntity.setTargetMethod(tempTccParticipator.getConfirmMethod());
                                tccExecuteEntity.setParamArgs(JsonUtils.json2obj(tempTccParticipator.getParamJson(),Object[].class));
                                tempNettyMsg.setData(JsonUtils.obj2json(tccExecuteEntity));
                                channelHandlerContext.writeAndFlush(JsonUtils.obj2json(tempNettyMsg));
                            }else {
                                TccExecuteEntity tccExecuteEntity = new TccExecuteEntity();
                                tccExecuteEntity.setTccId(tccController.getTccId());
                                tccExecuteEntity.setTargetClass(tempTccParticipator.getTargetClass());
                                ClientNettyMsg tempNettyMsg = new ClientNettyMsg();

                                tempNettyMsg.setMsgType(NettyMsgTypeEnum.CLIENT_LOCAL_EXECUTE.getMsgType());
                                tccExecuteEntity.setTargetMethod(tempTccParticipator.getCancelMethod());
                                tccExecuteEntity.setParamArgs(JsonUtils.json2obj(tempTccParticipator.getParamJson(),Object[].class));
                                tempNettyMsg.setData(JsonUtils.obj2json(tccExecuteEntity));
                                channelHandlerContext.writeAndFlush(JsonUtils.obj2json(tempNettyMsg));
                            }
                        }
                    }
                }
            }
        }else {
            if(EMPTY_DATA_COUNT.incrementAndGet() >= MAX_EMPTY_DATA){
                EMPTY_DATA_COUNT.set(0);
                Thread.sleep(RandomUtil.getSpecifiedRangeRandom(2,5)*1000);
            }
        }

    }
}
