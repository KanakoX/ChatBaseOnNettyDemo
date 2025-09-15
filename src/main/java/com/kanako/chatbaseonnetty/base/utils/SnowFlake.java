package com.kanako.chatbaseonnetty.base.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SnowFlake {
    @Bean
    public Snowflake snowflake() {
        return IdUtil.getSnowflake();
    }
}
