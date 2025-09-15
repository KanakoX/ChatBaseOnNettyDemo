package com.kanako.chatbaseonnetty.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kanako.chatbaseonnetty.base.service.SnowFlakeService;
import com.kanako.chatbaseonnetty.user.dao.mapper.UserMapper;
import com.kanako.chatbaseonnetty.user.pojo.dto.LoginDTO;
import com.kanako.chatbaseonnetty.user.pojo.po.UserPO;
import com.kanako.chatbaseonnetty.user.pojo.vo.UserVO;
import com.kanako.chatbaseonnetty.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SnowFlakeService snowFlakeService;

    @Override
    public UserVO login(LoginDTO loginDTO) {
        LambdaQueryWrapper<UserPO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserPO::getUsername, loginDTO.getUsername());
        UserPO userPO = userMapper.selectOne(lambdaQueryWrapper);
        if (userPO == null) throw new RuntimeException("用户不存在");
        if (!userPO.getPassword().equals(loginDTO.getPassword())) throw new RuntimeException("密码错误");
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userPO, userVO);
        return userVO;
    }

    @Override
    public void register(LoginDTO loginDTO) {
        LambdaQueryWrapper<UserPO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserPO::getUsername, loginDTO.getUsername());
        UserPO userPO = userMapper.selectOne(lambdaQueryWrapper);
        if (userPO != null) throw new RuntimeException("用户已存在");
        UserPO userPO1 = new UserPO();
        userPO1.setId(snowFlakeService.generateId("usr"));
        userPO1.setUsername(loginDTO.getUsername());
        userPO1.setPassword(loginDTO.getPassword());
        userPO1.setUpdatedAt(new Date());
        userMapper.insert(userPO1);
    }
}
