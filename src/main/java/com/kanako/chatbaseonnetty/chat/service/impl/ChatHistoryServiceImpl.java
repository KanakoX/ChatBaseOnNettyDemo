package com.kanako.chatbaseonnetty.chat.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.kanako.chatbaseonnetty.chat.pojo.po.ChatMessage;
import com.kanako.chatbaseonnetty.chat.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveMessage(ChatMessage chatMessage) {
        String messageJson = JSONObject.toJSONString(chatMessage);
        redisTemplate.opsForList().rightPush("room1", messageJson);
        redisTemplate.opsForList().trim("room1", 0, 99);
    }

    @Override
    public List<ChatMessage> getHistoryMessages(String room) {
        List<Object> chatMessageList = redisTemplate.opsForList().range(room, 0, 99);
        if (chatMessageList == null || chatMessageList.isEmpty()) {
            return Collections.emptyList();
        }
        List<ChatMessage> list = new ArrayList<>();
        for (Object object : chatMessageList) {
            if (object instanceof String jsonString) {
                ChatMessage chatMessage = JSONObject.parseObject(jsonString, ChatMessage.class);
                list.add(chatMessage);
            }
        }
        return list;
    }
}
