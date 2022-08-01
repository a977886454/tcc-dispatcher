package com.wu.netty;

import com.wu.constant.NettyConstant;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @Description: 在netty服务起来后，有channel新通道注册到EvenLoop上时在channel初始化时添加一些事件处理
 * @Author: wuzhouwei
 * @Date: 2022/3/10
 * @return:
 **/
public class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new LengthFieldBasedFrameDecoder(NettyConstant.MAX_FRAME_LENGTH,
                NettyConstant.LENGTH_FIELD_OFFSET, NettyConstant.LENGTH_FIELD_LENGTH,
                NettyConstant.LENGTH_ADJUSTMENT, NettyConstant.INITIAL_BYTES_TO_STRIP,false));
        // 消息编码:将消息封装为消息头和消息体,在消息前添加消息体的长度
        // 在往外发送消息时，封装上消息体的长度
        pipeline.addLast(new LengthFieldPrepender(4));

        // 添加Netty空闲超时检查的支持
        pipeline.addLast(new IdleStateHandler(NettyConstant.READ_IDLE_TIMEOUT,
                NettyConstant.WRITE_IDLE_TIMEOUT,
                NettyConstant.READ_WRITE_IDLE_TIMEOUT, TimeUnit.SECONDS));

        pipeline.addLast(new HeartBeatHandler());

        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());

        // 添加自定义的handler
        pipeline.addLast(new ChannelInboundEventHandler());

    }
}
