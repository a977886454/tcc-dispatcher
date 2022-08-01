package com.wu.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * @author wuzhouwei
 * @date 2022/3/10
 */
@Component
@Slf4j
public class NettyServer {

    public void start(InetSocketAddress address){
        // 主线程池
        EventLoopGroup bossGroup = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        // 工作线程池
        EventLoopGroup workerGroup = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        try {
            // 服务器
            ServerBootstrap server = new ServerBootstrap();
            // 指定server使用的线程组
            server.group(bossGroup, workerGroup)
                    // 指定通道类型
                    .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .childHandler(new NettyChannelInitializer())
                    //服务端接受连接的队列长度，如果队列已满，客户端连接将被拒绝
//                    .option(ChannelOption.SO_BACKLOG, 1024 * 10)
                    // 加快ACK确认,优化
                    .option(EpollChannelOption.TCP_QUICKACK, Boolean.TRUE)
                    // 启动TCP_NODELAY, 关闭Nagle算法(该算法是操作大数据包的优化)  优化小数据包
                    .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);

            // 绑定端口并且进行同步
            ChannelFuture future = server.bind(address).sync();
            log.info("netty服务器开始监听端口：{}", address.getPort());
            // 对关闭通道进行监听
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 资源关闭
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
