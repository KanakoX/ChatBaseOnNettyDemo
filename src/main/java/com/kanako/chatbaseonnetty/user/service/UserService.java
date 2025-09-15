package com.kanako.chatbaseonnetty.user.service;

import com.kanako.chatbaseonnetty.user.pojo.dto.LoginDTO;
import com.kanako.chatbaseonnetty.user.pojo.vo.UserVO;

public interface UserService {
    UserVO login(LoginDTO loginDTO);

    void register(LoginDTO loginDTO);
}
