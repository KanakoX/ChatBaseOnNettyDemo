package com.kanako.chatbaseonnetty.base.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NettyServer {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    @Value("${netty.port}")
    private int nettyPort;

    private EventLoopGroup bossGroup;  // 接收连接
    private EventLoopGroup workerGroup;  // 处理IO操作
    private ChannelFuture channelFuture;

    @PostConstruct
    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)  //
                .option(ChannelOption.SO_BACKLOG, 128)  // 连接队列大小
                .childOption(ChannelOption.SO_KEEPALIVE, true)  // 保持长连接
                .childHandler(new WebSocketServerInitializer());  // WebSocket初始化
        // 绑定端口
        channelFuture = bootstrap.bind(nettyPort).sync();
        if (channelFuture.isSuccess()) {
            log.info("Netty Server started on port {}", nettyPort);
        }
    }

    @PreDestroy
    public void stop() {
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        System.out.println("NettyServer stopped");
    }
}
