package com.wu.netty;

import com.wu.config.SpringContextHolder;
import com.wu.constant.NettyConstant;
import com.wu.entity.ClientNettyMsg;
import com.wu.enums.NettyMsgTypeEnum;
import com.wu.strategy.ClientNettyMsgHandleStrategyContext;
import com.wu.untils.JsonUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wuzhouwei
 * @date 2022/3/10
 */
@Slf4j
public class NettyClient {
    private volatile Channel channel;
    private Bootstrap bootstrap;
    private NioEventLoopGroup workGroup = new NioEventLoopGroup(4);
    private ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext;

    public NettyClient(String host, int port) {
        start(host, port);
    }

    public NettyClient(ClientNettyMsgHandleStrategyContext clientNettyMsgHandleStrategyContext) {
        this.clientNettyMsgHandleStrategyContext = clientNettyMsgHandleStrategyContext;
    }

    public NettyClient() {
    }

    public Channel getChannel() {
        return channel;
    }

    public void start(String host, int port) {
        try {
            bootstrap = new Bootstrap();
            bootstrap
                    .group(workGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(host, port)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws URISyntaxException {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(NettyConstant.MAX_FRAME_LENGTH,
                                    NettyConstant.LENGTH_FIELD_OFFSET, NettyConstant.LENGTH_FIELD_LENGTH,
                                    NettyConstant.LENGTH_ADJUSTMENT, NettyConstant.INITIAL_BYTES_TO_STRIP,false));
                            // 消息编码:将消息封装为消息头和消息体,在消息前添加消息体的长度
                            ch.pipeline().addLast(new LengthFieldPrepender(4));
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new IdleStateHandler(NettyConstant.READ_IDLE_TIMEOUT,
                                    NettyConstant.WRITE_IDLE_TIMEOUT,
                                    NettyConstant.READ_WRITE_IDLE_TIMEOUT, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new NettyClientHandler(NettyClient.this));
                        }
                    });
            doConnect(host, port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void doConnect(String host, int port) {
        if (channel != null && channel.isActive()) {
            return;
        }
        ChannelFuture future = bootstrap.connect(host, port);
        future.addListener((ChannelFutureListener) futureListener -> {
            if (futureListener.isSuccess()) {
                channel = futureListener.channel();
                ClientNettyMsgHandleStrategyContext.channel = channel;
                ClientNettyMsg clientNettyMsg = new ClientNettyMsg();
                clientNettyMsg.setMsgType(NettyMsgTypeEnum.BUILD_CONNECTION.getMsgType());
                clientNettyMsg.setData(SpringContextHolder.getApplicationName());
                clientNettyMsgHandleStrategyContext.sendBuildConnectionMessage(JsonUtils.obj2json(clientNettyMsg),SpringContextHolder.getApplicationName());
                log.info("Connect to server successfully!");
            } else {
                channel = null;
                log.info("Failed to connect to server, try connect after 10s");
                futureListener.channel().eventLoop().schedule(() -> doConnect(host, port), 10, TimeUnit.SECONDS);
            }
        });
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        NettyClient bootstrap = new NettyClient(null, NettyConstant.NETTY_SERVER_BIND_PORT);
        Thread.sleep(1000);
        Channel channel = bootstrap.getChannel();
        for (int i = 0; i < 10000; i++) {
            sendMsg(channel);
        }
    }

    public static void sendMsg(Channel channel) {
//        ClientNettyMsg scanClientNettyMessage = new ClientNettyMsg();
//        scanClientNettyMessage.setMsgType(NettyMsgTypeEnum.CHAT.getMsgType());
//        ChatMsg chatMsg = new ChatMsg();
//        chatMsg.setMsg("你好");
//        chatMsg.setUserId("1");
//        chatMsg.setFriendId("2");
//        scanClientNettyMessage.setData(JsonUtils.obj2json(chatMsg));
//        String mgs = JsonUtils.obj2json(scanClientNettyMessage);
//        channel.writeAndFlush(mgs);
    }
}
