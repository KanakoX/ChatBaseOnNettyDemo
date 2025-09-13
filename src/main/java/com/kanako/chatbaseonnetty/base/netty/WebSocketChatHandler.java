package com.kanako.chatbaseonnetty.base.netty;

import com.alibaba.fastjson2.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class WebSocketChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(WebSocketChatHandler.class);
    // 记录管理所有客户端的Channel
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    // 存储用户与channel的映射
    private static final Map<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();
    // 传入的用户ID
    private final String userId;

    public WebSocketChatHandler(String userId) {
        this.userId = userId;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String text = msg.text();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "CHAT");
        jsonObject.put("sender", userId);
        jsonObject.put("data", text);
        jsonObject.put("timestamp", new Date());
        broadcastAllChannels(jsonObject.toString());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete) {
            String requestUri = handshakeComplete.requestUri();
            log.info("Handshake completed: {} to {}", userId, requestUri);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive()) {
            handleInit(ctx);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handleInit(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        handleDestroy(ctx);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "STATUS");
        jsonObject.put("sender", "System");
        jsonObject.put("userId", userId);
        jsonObject.put("data", "leave");
        jsonObject.put("timestamp", new Date());
        jsonObject.put("currentUsers", USER_CHANNEL_MAP.keySet());
        broadcastAllChannels(jsonObject.toString());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive()) {
            handleInit(ctx);
        } else {
            handleDestroy(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught", cause);
        handleDestroy(ctx);
        ctx.close();
    }

    private void handleInit(ChannelHandlerContext ctx) {
        ctx.executor().schedule(() -> {
            Channel channel = ctx.channel();
            USER_CHANNEL_MAP.put(userId, channel);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "STATUS");
            jsonObject.put("sender", "System");
            jsonObject.put("userId", userId);
            jsonObject.put("data", "join");
            jsonObject.put("timestamp", new Date());
            jsonObject.put("currentUsers", USER_CHANNEL_MAP.keySet());
            /*channel.writeAndFlush(new TextWebSocketFrame(jsonObject.toString())).addListener(future -> {
                if (future.isSuccess()) {
                    log.info("{} has been sent to the server", channel.remoteAddress());
                } else {
                    log.error("Failed to send message to the server", future.cause());
                }
            });*/
            channels.add(channel);
            broadcastAllChannels(jsonObject.toString());
        }, 500, TimeUnit.MILLISECONDS);
    }

    private void handleDestroy(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        USER_CHANNEL_MAP.remove(userId);
        channels.remove(channel);
    }

    private void broadcastAllChannels(String message) {
        for (Channel  channel : channels) {
            channel.writeAndFlush(new TextWebSocketFrame(message));
        }
    }
}
