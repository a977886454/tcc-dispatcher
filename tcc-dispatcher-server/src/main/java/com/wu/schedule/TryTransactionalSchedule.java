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
import com.wu.utils.RandomUtil;
import io.netty.channel.ChannelHandlerContext;
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
                .lt(TccController::getLastTime, LocalDateTime.now())
                .ne(TccController::getStatus,0));

        List<TccParticipator> tccParticipators = tccParticipatorMapper.selectList(new LambdaQueryWrapper<TccParticipator>()
                .lt(TccParticipator::getRetriedCount, TccServerConstant.maxRetryCount)
                .lt(TccParticipator::getLastTime, LocalDateTime.now()));

        Map<Integer,Integer> alreadyMap = new HashMap<>();

        Map<String, List<TccParticipator>> tccParticipatorMap = FunctionalUtil.simpleGroupingBy(tccParticipators, TccParticipator::getTccId);
        if(!CollectionUtils.isEmpty(tccControllers)){
            for (TccController tccController : tccControllers) {

                List<TccParticipator> tempTccParticipators = tccParticipatorMap.get(tccController.getTccId());
                if(CollectionUtils.isEmpty(tempTccParticipators)){

                    ChannelHandlerContext channelHandlerContext = clientNettyMsgHandleStrategyContext.get(tccController.getApplicationName());
                    if(channelHandlerContext != null){
                        // TODO tcc?????????????????????????????????????????????,??????????????????????????????????????????????????????
                        if(tccController.getStatus() == 0){
                            //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

                            // ??????confirm
                        }else if(tccController.getStatus() == 1){
                            // ??????rpc??????????????????
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
                            // ??????rpc??????????????????
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
                    for (TccParticipator tempTccParticipator : tempTccParticipators) {
                        ChannelHandlerContext channelHandlerContext = clientNettyMsgHandleStrategyContext.get(tempTccParticipator.getApplicationName());
                        alreadyMap.put(tempTccParticipator.getId(),tempTccParticipator.getId());
                        if(channelHandlerContext != null){
                            if(tccController.getStatus() == 0){
                                if(tempTccParticipator.getStatus() == 2){
                                    TccController updateTccController = new TccController();
                                    updateTccController.setStatus(2);
                                    updateTccController.setTccId(tccController.getTccId());
                                    tccControllerMapper.updateById(updateTccController);
                                }

                                // TODO ????????????????????????????????????try?????????????????????????????????????????????server?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

                            }else if(tccController.getStatus() == 1){
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
