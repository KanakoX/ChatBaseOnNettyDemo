package com.kanako.chatbaseonnetty.base.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

public class WebSocketRequestRouter extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        final String uri = msg.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        String path = decoder.path();
        Map<String, List<String>> params = decoder.parameters();
        ChannelPipeline pipeline = ctx.pipeline();

        if (path.equals("/ws/chat")) {
            pipeline.addLast(new WebSocketServerProtocolHandler(
                    "/ws/chat", null, true, 65536 * 10, false, true
            ));
            String param = "";
            if (params.containsKey("userId")) param = params.get("userId").get(0);
            pipeline.addLast(new WebSocketChatHandler(param));
        } else {
            msg.setUri("/404");
        }

        ctx.fireChannelRead(msg.retain());
    }
}
