package com.wu.strategy;

import com.wu.config.SpringContextHolder;
import com.wu.context.TccContext;
import com.wu.context.TccContextLocal;
import com.wu.entity.ClientNettyMsg;
import com.wu.entity.TccClearEntity;
import com.wu.entity.TccExecuteEntity;
import com.wu.entity.TccMethodEntity;
import com.wu.enums.NettyMsgTypeEnum;
import com.wu.enums.TccRoleEnum;
import com.wu.init.InitTcc;
import com.wu.untils.JsonUtils;
import com.wu.untils.WuAopUtils;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.locks.LockSupport;

/**
 * @author wuzhouwei
 * @date 2022/7/29
 * 执行tcc发起方法或comfirm或cancel
 */
@Slf4j
public class ClientLocalExecuteTryStrategy implements ClientExecuteTryStrategy {
    @Override
    public void handle(ChannelHandlerContext ctx, ClientNettyMsg clientNettyMsg) {
        try {

            TccExecuteEntity tccExecuteEntity = JsonUtils.json2obj(clientNettyMsg.getData(),TccExecuteEntity.class);
            Thread thread = TccContextLocal.getConcurrentWaitThreadMap().get(tccExecuteEntity.getTccId());
            if(thread != null){
                TccContextLocal.getConcurrentWaitThreadMap().remove(clientNettyMsg.getData());
                LockSupport.unpark(thread);
            }
            TccContext tccContext = new TccContext();
            tccContext.setParamArgs(tccExecuteEntity.getParamArgs());
            tccContext.setTccRoleEnum(TccRoleEnum.RPC);
            tccContext.setTccId(tccExecuteEntity.getTccId());
            TccMethodEntity tccMethodEntity = InitTcc.getTccMethodEntity(tccExecuteEntity,tccContext);
            Object bean = tccMethodEntity.getBean();
            Method method = tccMethodEntity.getMethod();
            method.invoke(bean,tccExecuteEntity.getParamArgs());

            ClientNettyMsg clearTcc = new ClientNettyMsg();
            clearTcc.setMsgType(NettyMsgTypeEnum.SERVER_CLEAR_TCC.getMsgType());
            TccClearEntity tccClearEntity = new TccClearEntity();
            tccClearEntity.setApplicationName(SpringContextHolder.getApplicationName());
            tccClearEntity.setTargetClass(tccExecuteEntity.getTargetClass());
            tccClearEntity.setTccId(tccExecuteEntity.getTccId());
            clearTcc.setData(JsonUtils.obj2json(tccClearEntity));

            ctx.writeAndFlush(JsonUtils.obj2json(clearTcc));
        } catch (Exception e){
            log.info("",e);
        }finally {
            TccContextLocal.getInstance().remove();
        }
    }
}
