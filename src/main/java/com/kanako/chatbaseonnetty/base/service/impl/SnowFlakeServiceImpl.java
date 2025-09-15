package com.kanako.chatbaseonnetty.base.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.kanako.chatbaseonnetty.base.service.SnowFlakeService;
import com.kanako.chatbaseonnetty.base.utils.Base62Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SnowFlakeServiceImpl implements SnowFlakeService {
    @Autowired
    private Snowflake snowflake;

    @Override
    public String generateId(String prefix) {
        return prefix + Base62Utils.encode(snowflake.nextId());
    }
}
