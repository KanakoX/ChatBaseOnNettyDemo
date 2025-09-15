package com.kanako.chatbaseonnetty.user.controller;

import com.kanako.chatbaseonnetty.base.response.JsonResult;
import com.kanako.chatbaseonnetty.user.pojo.dto.LoginDTO;
import com.kanako.chatbaseonnetty.user.pojo.vo.UserVO;
import com.kanako.chatbaseonnetty.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public JsonResult login(@RequestBody LoginDTO loginDTO) {
        UserVO userVO = userService.login(loginDTO);
        return JsonResult.success(userVO);
    }

    @PostMapping("/register")
    public JsonResult register(@RequestBody LoginDTO loginDTO) {
        userService.register(loginDTO);
        return JsonResult.success(null);
    }
}
