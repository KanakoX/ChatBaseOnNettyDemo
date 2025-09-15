package com.kanako.chatbaseonnetty.user.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kanako.chatbaseonnetty.user.pojo.po.UserPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserPO> {
}
