package com.wu.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wu.entity.ClientNettyMsg;
import com.wu.entity.TccClearEntity;
import com.wu.entity.TccController;
import com.wu.entity.TccParticipator;
import com.wu.mapper.TccControllerMapper;
import com.wu.mapper.TccParticipatorMapper;
import com.wu.untils.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author wuzhouwei
 * @date 2022/7/29
 */
@Component
public class TransactionalClearStrategy implements TransactionalHandle {
    @Resource
    private TccControllerMapper tccControllerMapper;
    @Resource
    private TccParticipatorMapper tccParticipatorMapper;

    @Override
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
    public void handle(ChannelHandlerContext ctx, ClientNettyMsg clientNettyMsg) {
        TccClearEntity tccClearEntity = JsonUtils.json2obj(clientNettyMsg.getData(), TccClearEntity.class);
        tccParticipatorMapper.delete(new LambdaQueryWrapper<TccParticipator>()
                .eq(TccParticipator::getTccId,tccClearEntity.getTccId())
                .eq(TccParticipator::getApplicationName,tccClearEntity.getApplicationName())
                .eq(TccParticipator::getTargetClass,tccClearEntity.getTargetClass()));
        if(tccParticipatorMapper.selectCount(new LambdaQueryWrapper<TccParticipator>().eq(TccParticipator::getTccId,tccClearEntity.getTccId())) == 0){
            tccControllerMapper.delete(new LambdaQueryWrapper<TccController>()
                    .eq(TccController::getTccId,tccClearEntity.getTccId())
                    .eq(TccController::getApplicationName,tccClearEntity.getApplicationName())
                    .eq(TccController::getTargetClass,tccClearEntity.getTargetClass()));
        }
    }
}
