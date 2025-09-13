package com.kanako.chatbaseonnetty.user.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("user_info")
public class UserPO {
    private String id;
    private String username;
    private String password;
    private Date updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
