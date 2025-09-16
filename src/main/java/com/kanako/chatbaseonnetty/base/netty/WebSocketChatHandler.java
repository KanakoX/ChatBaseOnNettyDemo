package com.kanako.chatbaseonnetty.base.netty;

import com.alibaba.fastjson2.JSONObject;
import com.kanako.chatbaseonnetty.chat.pojo.po.ChatMessage;
import com.kanako.chatbaseonnetty.chat.service.ChatHistoryService;
import com.kanako.chatbaseonnetty.user.dao.mapper.UserMapper;
import com.kanako.chatbaseonnetty.user.pojo.po.UserPO;
import com.kanako.chatbaseonnetty.user.pojo.vo.UserVO;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class WebSocketChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    private static ChatHistoryService chatHistoryService;
    private static UserMapper userMapper;

    private static final Logger log = LoggerFactory.getLogger(WebSocketChatHandler.class);
    // 记录管理所有客户端的Channel
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    // 存储用户ID与channel的映射
    private static final Map<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();
    // 存储用户ID与UerVO的映射
    private static final Map<String, UserVO>  USER_VO_MAP = new ConcurrentHashMap<>();
    // 传入的用户ID
    private final String userId;

    public WebSocketChatHandler(String userId) {
        this.userId = userId;
    }

    public WebSocketChatHandler() {
        this.userId = null;
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketChatHandler.userMapper = userMapper;
    }
    @Autowired
    public void setChatHistoryService(ChatHistoryService chatHistoryService) {
        WebSocketChatHandler.chatHistoryService = chatHistoryService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        if (userId == null) throw new RuntimeException("用户发生错误");
        String text = msg.text();

        // 创建消息对象
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderInfo(USER_VO_MAP.get(userId));
        chatMessage.setContent(text);
        chatMessage.setCreatedAt(new Date());
        // 保存到Redis
        chatHistoryService.saveMessage(chatMessage);

        // 广播消息
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "CHAT");
        jsonObject.put("senderInfo", USER_VO_MAP.get(userId));
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
        if (userId == null) return;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "STATUS");
        jsonObject.put("sender", "System");
        jsonObject.put("userInfo", USER_VO_MAP.get(userId));
        jsonObject.put("data", "leave");
        jsonObject.put("timestamp", new Date());
        handleDestroy(ctx);
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
        if (userId == null) return;
        ctx.executor().schedule(() -> {
            Channel channel = ctx.channel();

            // 获取并发送历史消息
            List<ChatMessage> list = chatHistoryService.getHistoryMessages("room1");
            if (list != null && !list.isEmpty()) {
                JSONObject historyJsonObject = new JSONObject();
                historyJsonObject.put("type", "HISTORY");
                historyJsonObject.put("data", list);
                historyJsonObject.put("timestamp", new Date());
                channel.writeAndFlush(new TextWebSocketFrame(historyJsonObject.toString()));
            }

            USER_CHANNEL_MAP.put(userId, channel);
            USER_VO_MAP.put(userId, getUserInfo(userId));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "STATUS");
            jsonObject.put("sender", "System");
            jsonObject.put("userInfo", USER_VO_MAP.get(userId));
            jsonObject.put("data", "join");
            jsonObject.put("timestamp", new Date());
            jsonObject.put("currentUsers", USER_CHANNEL_MAP.keySet());
            channels.add(channel);
            broadcastAllChannels(jsonObject.toString());
        }, 500, TimeUnit.MILLISECONDS);
    }

    private void handleDestroy(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        USER_CHANNEL_MAP.remove(userId);
        USER_VO_MAP.remove(userId);
        channels.remove(channel);
    }

    private void broadcastAllChannels(String message) {
        for (Channel channel : channels) {
            channel.writeAndFlush(new TextWebSocketFrame(message));
        }
    }

    private UserVO getUserInfo(String userId) {
        UserPO userPO = userMapper.selectById(userId);
        if (userPO == null) return null;
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userPO, userVO);
        return userVO;
    }
}
