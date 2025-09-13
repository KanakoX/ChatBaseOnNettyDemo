package com.kanako.chatbaseonnetty.base.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 添加HTTP编解码器
        pipeline.addLast(new HttpServerCodec());
        // 支持大数据流
        pipeline.addLast(new ChunkedWriteHandler());
        // 聚合HTTP消息
        pipeline.addLast(new HttpObjectAggregator(65536));
        // 自定义请求路由处理器
        pipeline.addLast(new WebSocketRequestRouter());
    }
}
