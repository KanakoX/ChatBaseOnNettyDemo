package com.kanako.chatbaseonnetty.base.config;

import com.kanako.chatbaseonnetty.base.netty.WebSocketRequestRouter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        WebSocketRequestRouter.setApplicationContext(applicationContext);
    }
}
