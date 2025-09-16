package com.kanako.chatbaseonnetty.chat.service;

import com.kanako.chatbaseonnetty.chat.pojo.po.ChatMessage;

import java.util.List;

public interface ChatHistoryService {
    void saveMessage(ChatMessage chatMessage);

    List<ChatMessage> getHistoryMessages(String room1);
}
