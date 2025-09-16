package com.kanako.chatbaseonnetty.chat.pojo.po;

import com.kanako.chatbaseonnetty.user.pojo.vo.UserVO;

import java.util.Date;

public class ChatMessage {
    private String id;
    private UserVO senderInfo;
    private String content;
    private Date createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserVO getSenderInfo() {
        return senderInfo;
    }

    public void setSenderInfo(UserVO senderInfo) {
        this.senderInfo = senderInfo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
